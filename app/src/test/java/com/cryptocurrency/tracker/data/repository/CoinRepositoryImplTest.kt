package com.cryptocurrency.tracker.data.repository

import app.cash.turbine.test
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.data.local.CoinDao
import com.cryptocurrency.tracker.data.local.CoinEntity
import com.cryptocurrency.tracker.data.remote.ApiService
import com.cryptocurrency.tracker.data.remote.dto.CoinDto
import com.cryptocurrency.tracker.data.remote.websocket.BinanceTickerDto
import com.cryptocurrency.tracker.data.remote.websocket.BinanceWebSocketClient
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CoinRepositoryImplTest {

    private lateinit var repository: CoinRepositoryImpl

    @MockK
    private lateinit var api: ApiService

    @MockK
    private lateinit var dao: CoinDao

    @MockK
    private lateinit var webSocketClient: BinanceWebSocketClient

    private val testDispatcher = StandardTestDispatcher()
    
    private val tickerFlow = MutableSharedFlow<BinanceTickerDto>()
    private val isConnectedFlow = MutableStateFlow(value = false)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        
        every { webSocketClient.tickerFlow } returns tickerFlow
        every { webSocketClient.isConnected } returns isConnectedFlow
        every { webSocketClient.connect(any()) } just Runs
        
        repository = CoinRepositoryImpl(api, dao, webSocketClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getCoins emits loading then success when api call is successful`() = runTest {
        val coinEntities = listOf(
            CoinEntity("bitcoin", "Bitcoin", "BTC", "", 50000.0, 1.0),
        )
        val coinDtos = listOf(
            CoinDto("bitcoin", "BTC", "Bitcoin", "", 51000.0, 2.0)
        )
        
        coEvery { dao.getAllCoins() } returnsMany listOf(coinEntities, coinEntities.map { it.copy(priceUsd = 51000.0) })
        coEvery { api.getCoins() } returns coinDtos
        coEvery { dao.deleteCoins() } just Runs
        coEvery { dao.insertCoins(any()) } just Runs

        repository.getCoins().test {
            val firstEmission = awaitItem()
            assertTrue(firstEmission is Resource.Loading)
            
            val secondEmission = awaitItem()
            assertTrue(secondEmission is Resource.Loading)
            assertEquals(50000.0, secondEmission.data?.first()?.priceUsd)

            val thirdEmission = awaitItem()
            assertTrue(thirdEmission is Resource.Success)
            assertEquals(51000.0, thirdEmission.data?.first()?.priceUsd)
            
            verify { webSocketClient.connect(listOf("BTC")) }
            awaitComplete()
        }
    }

    @Test
    fun `getCoins emits loading then error when api call fails with HttpException`() = runTest {
        val coinEntities = listOf(
            CoinEntity("bitcoin", "Bitcoin", "BTC", "", 50000.0, 1.0),
        )
        
        coEvery { dao.getAllCoins() } returns coinEntities
        val httpException = mockk<HttpException>()
        coEvery { api.getCoins() } throws httpException

        repository.getCoins().test {
            assertTrue(awaitItem() is Resource.Loading)
            assertTrue(awaitItem() is Resource.Loading)

            val errorEmission = awaitItem()
            assertTrue(errorEmission is Resource.Error)
            assertEquals("Oops, something went wrong!", errorEmission.message)
            assertEquals(50000.0, errorEmission.data?.first()?.priceUsd)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCoins emits loading then error when api call fails`() = runTest {
        val coinEntities = listOf(
            CoinEntity("bitcoin", "Bitcoin", "BTC", "", 50000.0, 1.0),
        )
        
        coEvery { dao.getAllCoins() } returns coinEntities
        coEvery { api.getCoins() } throws IOException("Network error")

        repository.getCoins().test {
            assertTrue(awaitItem() is Resource.Loading)
            assertTrue(awaitItem() is Resource.Loading)

            val errorEmission = awaitItem()
            assertTrue(errorEmission is Resource.Error)
            assertEquals("Couldn't reach server. Check your internet connection.", errorEmission.message)
            assertEquals(50000.0, errorEmission.data?.first()?.priceUsd)
            
            awaitComplete()
        }
    }

    @Test
    fun `observeCoins returns flow from dao`() = runTest {
        val coinEntities = listOf(
            CoinEntity("bitcoin", "Bitcoin", "BTC", "", 50000.0, 1.0),
        )
        every { dao.observeAllCoins() } returns flowOf(coinEntities)

        repository.observeCoins().test {
            val emission = awaitItem()
            assertEquals(1, emission.size)
            assertEquals("Bitcoin", emission.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ticker updates update dao`() = runTest {
        val ticker = BinanceTickerDto(
            eventType = "24hrTicker",
            eventTime = 123456789L,
            symbol = "BTCUSDT",
            price = "52000.0",
            priceChangePercent = "2.5"
        )
        
        coEvery { dao.updateCoinPrice(any(), any(), any(), any()) } just Runs

        // startHybridMonitor is called inside observeCoins
        every { dao.observeAllCoins() } returns flowOf(emptyList())
        repository.observeCoins().test {
            tickerFlow.emit(ticker)
            
            coVerify(timeout = 2000) {
                dao.updateCoinPrice(
                    symbol = "btc",
                    price = 52000.0,
                    changePercent = 2.5,
                    lastUpdate = 123456789L
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `REST polling starts when websocket is disconnected and stops on reconnect`() = runTest {
        isConnectedFlow.value = false
        coEvery { api.getCoins() } returns emptyList()
        coEvery { dao.insertCoins(any()) } just Runs
        every { dao.observeAllCoins() } returns flowOf(emptyList())

        repository.observeCoins().test {
            // Trigger hybrid monitor check (5s delay)
            advanceTimeBy(6000)
            coVerify(atLeast = 1) { api.getCoins() }
            
            // Reconnect
            isConnectedFlow.value = true
            advanceTimeBy(11000)
            
            // Should not poll again after reconnection (polling is every 10s)
            // If it didn't stop, we would have at least 2 calls
            coVerify(exactly = 1) { api.getCoins() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCoinById returns coin from dao`() = runTest {
        val coinEntity = CoinEntity("bitcoin", "Bitcoin", "BTC", "", 50000.0, 1.0)
        coEvery { dao.getCoinById("bitcoin") } returns coinEntity

        val result = repository.getCoinById("bitcoin")
        
        assertEquals("Bitcoin", result?.name)
        coVerify { dao.getCoinById("bitcoin") }
    }

    @Test
    fun `getCoinById returns null when coin not found`() = runTest {
        coEvery { dao.getCoinById("unknown") } returns null

        val result = repository.getCoinById("unknown")
        
        assertEquals(null, result)
        coVerify { dao.getCoinById("unknown") }
    }
}

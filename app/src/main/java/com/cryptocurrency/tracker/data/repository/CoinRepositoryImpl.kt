package com.cryptocurrency.tracker.data.repository

import com.cryptocurrency.tracker.data.local.CoinDao
import com.cryptocurrency.tracker.data.remote.ApiService
import com.cryptocurrency.tracker.data.remote.websocket.BinanceWebSocketClient
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException

class CoinRepositoryImpl(
    private val api: ApiService,
    private val dao: CoinDao,
    private val webSocketClient: BinanceWebSocketClient
) : CoinRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getCoins(): Flow<Resource<List<Coin>>> = flow {
        emit(Resource.Loading())
        val localCoins = dao.getAllCoins().map { it.toCoin() }
        emit(Resource.Loading(data = localCoins))

        try {
            val remoteCoins = api.getCoins()
            dao.deleteCoins()
            dao.insertCoins(remoteCoins.map { it.toCoinEntity() })
            val updatedLocalCoins = dao.getAllCoins().map { it.toCoin() }
            emit(Resource.Success(updatedLocalCoins))
            
            // Start WebSocket after initial fetch
            webSocketClient.connect(updatedLocalCoins.map { it.symbol })
        } catch (e: Exception) {
            val message = when (e) {
                is HttpException -> "Oops, something went wrong!"
                is IOException -> "Couldn't reach server. Check your internet connection."
                else -> e.message ?: "Unknown error"
            }
            emit(Resource.Error(message = message, data = localCoins))
        }
    }

    override fun observeCoins(): Flow<List<Coin>> {
        // Multi-Track Strategy: Hybrid WS + REST fallback
        startHybridMonitor()
        
        return dao.observeAllCoins().map { entities ->
            entities.map { it.toCoin() }
        }
    }

    private var pollingJob: Job? = null
    
    private fun startHybridMonitor() {
        repositoryScope.launch {
            webSocketClient.isConnected.collectLatest { isConnected ->
                if (isConnected) {
                    pollingJob?.cancel()
                } else {
                    delay(5000)
                    if (!webSocketClient.isConnected.value) {
                        startRestPolling()
                    }
                }
            }
        }

        // Process WebSocket Ticker Updates and persist to DB
        repositoryScope.launch {
            webSocketClient.tickerFlow.collect { ticker ->
                val symbol = ticker.symbol.replace("USDT", "").lowercase()
                // Sequence Consistency: Use eventTime to ensure we don't apply older updates
                dao.updateCoinPrice(
                    symbol = symbol,
                    price = ticker.price.toDoubleOrNull() ?: 0.0,
                    changePercent = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0,
                    lastUpdate = ticker.eventTime
                )
            }
        }
    }

    private fun startRestPolling() {
        pollingJob?.cancel()
        pollingJob = repositoryScope.launch {
            while (isActive) {
                try {
                    val remoteCoins = api.getCoins()
                    if (!webSocketClient.isConnected.value) {
                        dao.insertCoins(remoteCoins.map { it.toCoinEntity() })
                    }
                } catch (e: Exception) {
                    // Log error but keep polling
                }
                delay(10000) // Poll every 10 seconds
            }
        }
    }

    override suspend fun getCoinById(id: String): Coin? {
        return dao.getCoinById(id)?.toCoin()
    }
}

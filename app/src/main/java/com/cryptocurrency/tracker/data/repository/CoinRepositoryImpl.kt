package com.cryptocurrency.tracker.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cryptocurrency.tracker.data.local.CoinDao
import com.cryptocurrency.tracker.data.local.CoinDatabase
import com.cryptocurrency.tracker.data.remote.ApiService
import com.cryptocurrency.tracker.data.remote.paging.CoinRemoteMediator
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
    private val db: CoinDatabase,
    private val webSocketClient: BinanceWebSocketClient
) : CoinRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getCoins(): Flow<Resource<List<Coin>>> = flow {
        // ... (keep current implementation for compatibility or update it)
        emit(Resource.Loading())
        val localCoins = dao.getAllCoins().map { it.toCoin() }
        emit(Resource.Loading(data = localCoins))

        try {
            val remoteCoins = api.getCoins()
            // We might want to keep this for the non-paged parts of the app
            dao.deleteCoins()
            dao.insertCoins(remoteCoins.map { it.toCoinEntity() })
            val updatedLocalCoins = dao.getAllCoins().map { it.toCoin() }
            emit(Resource.Success(updatedLocalCoins))
        } catch (e: Exception) {
            val message = when (e) {
                is HttpException -> "Oops, something went wrong!"
                is IOException -> "Couldn't reach server. Check your internet connection."
                else -> e.message ?: "Unknown error"
            }
            emit(Resource.Error(message = message, data = localCoins))
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun observeCoinsPaged(): Flow<PagingData<Coin>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = CoinRemoteMediator(db, api),
            pagingSourceFactory = { dao.observeAllCoinsPaged() }
        ).flow.map { pagingData ->
            pagingData.map { it.toCoin() }
        }
    }

    override suspend fun updateWebSocketSubscriptions(symbols: List<String>) {
        if (symbols.isNotEmpty()) {
            webSocketClient.connect(symbols)
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
    
    @OptIn(FlowPreview::class)
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

        // Optimized persistence: Debounce DB writes to prevent excessive PagingSource invalidations
        repositoryScope.launch {
            webSocketClient.tickerFlow
                .groupBy { it.symbol }
                .collect { group ->
                    launch {
                        group.value
                            .debounce(5000) // Persist to DB at most every 5 seconds per coin
                            .collect { ticker ->
                                val symbol = ticker.symbol.replace("USDT", "").lowercase()
                                dao.updateCoinPrice(
                                    symbol = symbol,
                                    price = ticker.price.toDoubleOrNull() ?: 0.0,
                                    changePercent = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0,
                                    lastUpdate = ticker.eventTime
                                )
                            }
                    }
                }
        }
    }

    // Helper for debouncing updates per symbol
    private fun <T, K> Flow<T>.groupBy(keySelector: (T) -> K): Flow<GroupedFlow<K, T>> = flow {
        val groups = mutableMapOf<K, MutableSharedFlow<T>>()
        collect { value ->
            val key = keySelector(value)
            val flow = groups.getOrPut(key) {
                MutableSharedFlow<T>(extraBufferCapacity = 1).also {
                    emit(GroupedFlow(key, it))
                }
            }
            flow.emit(value)
        }
    }

    private data class GroupedFlow<K, T>(val key: K, val value: Flow<T>)

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

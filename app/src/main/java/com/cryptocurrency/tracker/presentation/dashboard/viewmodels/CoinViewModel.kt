package com.cryptocurrency.tracker.presentation.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.data.remote.websocket.BinanceWebSocketClient
import com.cryptocurrency.tracker.domain.use_case.GetCoinsUseCase
import com.cryptocurrency.tracker.domain.use_case.ObserveCoinsUseCase
import com.cryptocurrency.tracker.domain.use_case.UpdateCoinPriceUseCase
import com.cryptocurrency.tracker.presentation.dashboard.model.CoinFilter
import com.cryptocurrency.tracker.presentation.dashboard.model.CoinListState
import com.cryptocurrency.tracker.presentation.dashboard.model.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val getCoinsUseCase: GetCoinsUseCase,
    private val observeCoinsUseCase: ObserveCoinsUseCase,
    private val updateCoinPriceUseCase: UpdateCoinPriceUseCase,
    private val webSocketClient: BinanceWebSocketClient
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    
    // Live price updates held in memory for maximum performance (SDE-2 optimization)
    private val _livePrices = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap())
    val livePrices = _livePrices.asStateFlow()

    // Last update timestamp per coin for staleness tracking
    private val _lastUpdateMap = MutableStateFlow<Map<String, Long>>(emptyMap())

    // SharedFlow to buffer DB updates for debouncing
    private val _dbUpdateQueue = MutableSharedFlow<UpdateParams>(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private data class UpdateParams(val symbol: String, val price: Double, val changePercent: Double, val lastUpdate: Long)

    val state = combine(
        _state,
        observeCoinsUseCase(),
        _lastUpdateMap
    ) { state, coins, lastUpdates ->
        val currentTime = System.currentTimeMillis()
        val isStale = lastUpdates.values.any { currentTime - it > 15000 } || 
                     (lastUpdates.isEmpty() && !state.isLoading && state.connectionStatus == ConnectionStatus.OFFLINE)

        val filtered = when (state.selectedFilter) {
            CoinFilter.ALL -> coins
            CoinFilter.TOP_GAINERS -> coins.sortedByDescending { it.changePercent24Hr }
            CoinFilter.CHANGE_24H -> coins.sortedByDescending { abs(it.changePercent24Hr) }
            CoinFilter.TOP_50 -> coins.sortedByDescending { it.marketCap }.take(50)
            CoinFilter.MARKET_CAP -> coins.sortedByDescending { it.marketCap }
        }
        state.copy(coins = filtered, isStale = isStale)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CoinListState())

    private var loadJob: Job? = null

    init {
        loadCoins()
        observeWebSocketUpdates()
        processDbUpdates()
        monitorStaleness()
    }

    private fun monitorStaleness() {
        viewModelScope.launch {
            while (true) {
                // Trigger recomposition every 5s to check staleness
                _state.update { it.copy() }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun processDbUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            _dbUpdateQueue
                .groupBy { it.symbol }
                .collect { group ->
                    launch {
                        group.value
                            .debounce(2000)
                            .collect { params ->
                                updateCoinPriceUseCase(params.symbol, params.price, params.changePercent, params.lastUpdate)
                            }
                    }
                }
        }
    }

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

    fun onFilterSelected(filter: CoinFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun loadCoins() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getCoinsUseCase().collect { result ->
                _state.update { 
                    it.copy(
                        isLoading = result is Resource.Loading, 
                        error = if (result is Resource.Error) result.message else null
                    ) 
                }
            }
        }
    }

    private fun observeWebSocketUpdates() {
        // Monitor connection status
        viewModelScope.launch(Dispatchers.IO) {
            webSocketClient.isConnected.collect { isConnected ->
                _state.update { 
                    it.copy(
                        connectionStatus = if (isConnected) ConnectionStatus.CONNECTED else ConnectionStatus.RECONNECTING 
                    ) 
                }
            }
        }

        // Process ticker updates
        viewModelScope.launch(Dispatchers.IO) {
            webSocketClient.tickerFlow.collect { ticker ->
                val coinSymbol = ticker.symbol
                    .replace("USDT", "")
                    .lowercase()
                
                val price = ticker.price.toDoubleOrNull() ?: 0.0
                val change = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0
                
                // 1. Update In-Memory state
                _livePrices.update { it + (coinSymbol to (price to change)) }
                _lastUpdateMap.update { it + (coinSymbol to ticker.eventTime) }

                // 2. Queue for DB update
                _dbUpdateQueue.emit(UpdateParams(coinSymbol, price, change, ticker.eventTime))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}

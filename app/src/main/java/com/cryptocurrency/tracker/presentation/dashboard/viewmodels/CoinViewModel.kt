package com.cryptocurrency.tracker.presentation.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cryptocurrency.tracker.data.remote.websocket.BinanceWebSocketClient
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.use_case.ObserveCoinsPagedUseCase
import com.cryptocurrency.tracker.domain.use_case.UpdateWebSocketSubscriptionsUseCase
import com.cryptocurrency.tracker.presentation.dashboard.model.CoinFilter
import com.cryptocurrency.tracker.presentation.dashboard.model.CoinListState
import com.cryptocurrency.tracker.presentation.dashboard.model.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val observeCoinsPagedUseCase: ObserveCoinsPagedUseCase,
    private val updateWebSocketSubscriptionsUseCase: UpdateWebSocketSubscriptionsUseCase,
    private val webSocketClient: BinanceWebSocketClient
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    val state = _state.asStateFlow()

    val pagedCoins: Flow<PagingData<Coin>> = observeCoinsPagedUseCase()
        .cachedIn(viewModelScope)
    
    // Live price updates held in memory for maximum performance
    private val _livePrices = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap())
    val livePrices = _livePrices.asStateFlow()

    // Last update timestamp per coin for staleness tracking
    private val _lastUpdateMap = MutableStateFlow<Map<String, Long>>(emptyMap())
    val lastUpdateMap = _lastUpdateMap.asStateFlow()

    private val visibleSymbolsFlow = MutableStateFlow<List<String>>(emptyList())
    
    // Internal buffer for high-frequency updates
    private val pendingLivePrices = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap())
    private val pendingLastUpdates = MutableStateFlow<Map<String, Long>>(emptyMap())

    init {
        observeWebSocketUpdates()
        monitorStaleness()
        handleVisibleSymbols()
        syncThrottledUpdates()
    }

    private fun syncThrottledUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(200) // Max 5Hz UI updates to prevent frame drops
                if (pendingLivePrices.value.isNotEmpty()) {
                    val prices = pendingLivePrices.value
                    val updates = pendingLastUpdates.value
                    
                    _livePrices.update { it + prices }
                    _lastUpdateMap.update { it + updates }
                    
                    pendingLivePrices.value = emptyMap()
                    pendingLastUpdates.value = emptyMap()
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun handleVisibleSymbols() {
        viewModelScope.launch {
            visibleSymbolsFlow
                .debounce(1000) // Wait for 1s of stability before reconnecting WS
                .distinctUntilChanged()
                .collect { symbols ->
                    updateWebSocketSubscriptionsUseCase(symbols)
                }
        }
    }

    fun onVisibleCoinsChanged(symbols: List<String>) {
        visibleSymbolsFlow.value = symbols
    }

    private fun monitorStaleness() {
        viewModelScope.launch {
            while (true) {
                // Force check every 5s for staleness indicators
                _state.update { it.copy() }
                delay(5000)
            }
        }
    }

    fun onFilterSelected(filter: CoinFilter) {
        _state.update { it.copy(selectedFilter = filter) }
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

        // Process ticker updates for UI only
        viewModelScope.launch(Dispatchers.IO) {
            webSocketClient.tickerFlow.collect { ticker ->
                val coinSymbol = ticker.symbol
                    .replace("USDT", "")
                    .lowercase()
                
                val price = ticker.price.toDoubleOrNull() ?: 0.0
                val change = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0
                
                // Buffer for Throttled UI update (UI only)
                pendingLivePrices.update { it + (coinSymbol to (price to change)) }
                pendingLastUpdates.update { it + (coinSymbol to ticker.eventTime) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}

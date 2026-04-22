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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    val state = _state.combine(observeCoinsUseCase()) { state, coins ->
        val filtered = when (state.selectedFilter) {
            CoinFilter.ALL -> coins
            CoinFilter.TOP_GAINERS -> coins.sortedByDescending { it.changePercent24Hr }
            CoinFilter.CHANGE_24H -> coins.sortedByDescending { abs(it.changePercent24Hr) }
            CoinFilter.TOP_50 -> coins.sortedByDescending { it.marketCap }.take(50)
            CoinFilter.MARKET_CAP -> coins.sortedByDescending { it.marketCap }
        }
        state.copy(coins = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CoinListState())

    private var loadJob: Job? = null

    init {
        loadCoins()
        observeWebSocketUpdates()
    }

    fun onFilterSelected(filter: CoinFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun loadCoins() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getCoinsUseCase().collect { result ->
                _state.update { it.copy(isLoading = result is Resource.Loading, error = if (result is Resource.Error) result.message else null) }
                
                if (result is Resource.Success && result.data != null) {
                    val symbols = result.data.map { it.symbol }
                    webSocketClient.connect(symbols)
                }
            }
        }
    }

    private fun observeWebSocketUpdates() {
        viewModelScope.launch {
            webSocketClient.tickerFlow.collect { ticker ->
                val coinSymbol = ticker.symbol
                    .replace("USDT", "")
                    .lowercase()
                
                // 1. Update In-Memory state for immediate UI reaction (High frequency)
                val price = ticker.price.toDoubleOrNull() ?: 0.0
                val change = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0
                _livePrices.update { it + (coinSymbol to (price to change)) }

                // 2. Persist to DB periodically or in background (lower priority)
                updateCoinPriceUseCase(
                    symbol = coinSymbol,
                    price = price,
                    changePercent = change
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}

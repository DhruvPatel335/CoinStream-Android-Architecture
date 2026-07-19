package com.cryptocurrency.tracker.presentation.dashboard.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrency.tracker.data.remote.websocket.BinanceWebSocketClient
import com.cryptocurrency.tracker.domain.use_case.GetCoinUseCase
import com.cryptocurrency.tracker.presentation.dashboard.model.CoinDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val getCoinUseCase: GetCoinUseCase,
    private val webSocketClient: BinanceWebSocketClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CoinDetailState())
    val state = _state.asStateFlow()

    private var subscribedSymbol: String? = null

    init {
        savedStateHandle.get<String>("coinId")?.let { coinId ->
            getCoin(coinId)
        }
    }

    private fun getCoin(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val coin = getCoinUseCase(id)
            if (coin != null) {
                _state.update { it.copy(
                    coin = coin,
                    isLoading = false
                ) }
                subscribeToLiveUpdates(coin.symbol)
            } else {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Coin not found"
                ) }
            }
        }
    }

    private fun subscribeToLiveUpdates(symbol: String) {
        val normalized = symbol.lowercase()
        subscribedSymbol = normalized
        webSocketClient.ensureSubscribed(normalized)

        viewModelScope.launch(Dispatchers.IO) {
            webSocketClient.tickerFlow
                .filter { it.symbol.replace("USDT", "").lowercase() == normalized }
                .collect { ticker ->
                    val price = ticker.price.toDoubleOrNull()
                    val change = ticker.priceChangePercent.toDoubleOrNull()
                    if (price != null && change != null) {
                        _state.update { current ->
                            current.coin?.let { coin ->
                                current.copy(
                                    coin = coin.copy(
                                        priceUsd = price,
                                        changePercent24Hr = change,
                                        lastUpdate = ticker.eventTime
                                    )
                                )
                            } ?: current
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscribedSymbol?.let { webSocketClient.releaseSubscription(it) }
    }
}

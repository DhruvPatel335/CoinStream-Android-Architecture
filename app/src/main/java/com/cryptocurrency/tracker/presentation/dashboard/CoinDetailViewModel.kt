package com.cryptocurrency.tracker.presentation.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val repository: CoinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CoinDetailState())
    val state = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("coinId")?.let { coinId ->
            getCoin(coinId)
        }
    }

    private fun getCoin(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val coin = repository.getCoinById(id)
            if (coin != null) {
                _state.update { it.copy(
                    coin = coin,
                    isLoading = false
                ) }
            } else {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Coin not found"
                ) }
            }
        }
    }
}
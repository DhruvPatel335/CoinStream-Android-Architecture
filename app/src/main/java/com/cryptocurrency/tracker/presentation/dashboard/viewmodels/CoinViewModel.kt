package com.cryptocurrency.tracker.presentation.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.use_case.GetCoinsUseCase
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
    private val getCoinsUseCase: GetCoinsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    
    val state = _state.map { state ->
        val filtered = when (state.selectedFilter) {
            CoinFilter.ALL -> state.coins
            CoinFilter.TOP_GAINERS -> state.coins.sortedByDescending { it.changePercent24Hr }
            CoinFilter.CHANGE_24H -> state.coins.sortedByDescending { abs(it.changePercent24Hr) }
            CoinFilter.TOP_50 -> state.coins.sortedByDescending { it.marketCap }.take(50)
            CoinFilter.MARKET_CAP -> state.coins.sortedByDescending { it.marketCap }
        }
        state.copy(coins = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CoinListState())

    private var loadJob: Job? = null

    init {
        loadCoins()
    }

    fun onFilterSelected(filter: CoinFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun loadCoins() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getCoinsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(
                            coins = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        ) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(
                            coins = result.data ?: emptyList(),
                            isLoading = false,
                            error = result.message
                        ) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(
                            coins = result.data ?: emptyList(),
                            isLoading = true
                        ) }
                    }
                }
            }
        }
    }
}
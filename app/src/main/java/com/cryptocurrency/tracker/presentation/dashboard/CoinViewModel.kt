package com.cryptocurrency.tracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val repository: CoinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCoins()
    }

    fun loadCoins() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getCoins().collect { result ->
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
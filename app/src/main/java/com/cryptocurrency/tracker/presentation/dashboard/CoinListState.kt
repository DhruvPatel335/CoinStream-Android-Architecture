package com.cryptocurrency.tracker.presentation.dashboard

import com.cryptocurrency.tracker.domain.model.Coin

data class CoinListState(
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
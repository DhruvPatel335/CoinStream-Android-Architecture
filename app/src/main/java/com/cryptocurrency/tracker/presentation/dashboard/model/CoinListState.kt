package com.cryptocurrency.tracker.presentation.dashboard.model

import com.cryptocurrency.tracker.domain.model.Coin

data class CoinListState(
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: CoinFilter = CoinFilter.ALL
)
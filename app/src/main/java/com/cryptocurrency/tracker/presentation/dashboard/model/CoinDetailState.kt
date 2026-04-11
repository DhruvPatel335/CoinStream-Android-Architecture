package com.cryptocurrency.tracker.presentation.dashboard.model

import com.cryptocurrency.tracker.domain.model.Coin

data class CoinDetailState(
    val coin: Coin? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
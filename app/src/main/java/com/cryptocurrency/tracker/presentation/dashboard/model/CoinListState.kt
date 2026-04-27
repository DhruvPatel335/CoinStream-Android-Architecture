package com.cryptocurrency.tracker.presentation.dashboard.model

import com.cryptocurrency.tracker.domain.model.Coin

enum class ConnectionStatus {
    CONNECTED,
    RECONNECTING,
    OFFLINE
}

data class CoinListState(
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: CoinFilter = CoinFilter.ALL,
    val connectionStatus: ConnectionStatus = ConnectionStatus.RECONNECTING,
    val isStale: Boolean = false,
    val lastUpdateMap: Map<String, Long> = emptyMap()
)

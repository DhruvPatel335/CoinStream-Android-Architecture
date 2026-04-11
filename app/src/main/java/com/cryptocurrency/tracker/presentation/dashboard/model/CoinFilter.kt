package com.cryptocurrency.tracker.presentation.dashboard.model

enum class CoinFilter(val label: String) {
    ALL("All"),
    TOP_GAINERS("Top gainers"),
    CHANGE_24H("24h %"),
    TOP_50("Top 50"),
    MARKET_CAP("Market Cap")
}
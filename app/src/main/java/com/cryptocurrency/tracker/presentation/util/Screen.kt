package com.cryptocurrency.tracker.presentation.util

sealed class Screen(val route: String) {
    object CoinList: Screen("coin_list")
    object CoinDetail: Screen("coin_detail/{coinId}") {
        fun createRoute(coinId: String) = "coin_detail/$coinId"
    }
}
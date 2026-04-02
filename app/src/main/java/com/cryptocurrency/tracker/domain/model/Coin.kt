package com.cryptocurrency.tracker.domain.model

data class Coin(
    val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String,
    val priceUsd: Double,
    val changePercent24Hr: Double,
    val marketCap: Double = 0.0,
    val marketCapRank: Int = 0,
    val totalVolume: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val ath: Double = 0.0,
    val atl: Double = 0.0,
    val sparkline: List<Double> = emptyList()
)
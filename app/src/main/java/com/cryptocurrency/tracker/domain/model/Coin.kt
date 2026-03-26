package com.cryptocurrency.tracker.domain.model

data class Coin(
    val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String,
    val priceUsd: Double,
    val changePercent24Hr: Double,
    val sparkline: List<Double> = emptyList()
)
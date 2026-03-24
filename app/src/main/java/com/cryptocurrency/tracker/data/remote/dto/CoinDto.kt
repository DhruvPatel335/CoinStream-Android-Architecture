package com.cryptocurrency.tracker.data.remote.dto

import com.cryptocurrency.tracker.data.local.CoinEntity
import com.cryptocurrency.tracker.domain.model.Coin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("image") val imageUrl: String,
    @SerialName("current_price") val currentPrice: Double? = 0.0,
    @SerialName("price_change_percentage_24h") val priceChange24h: Double? = 0.0
) {
    fun toCoin(): Coin {
        return Coin(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            priceUsd = currentPrice ?: 0.0,
            changePercent24Hr = priceChange24h ?: 0.0
        )
    }

    fun toCoinEntity(): CoinEntity {
        return CoinEntity(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            priceUsd = currentPrice ?: 0.0,
            changePercent24Hr = priceChange24h ?: 0.0
        )
    }
}
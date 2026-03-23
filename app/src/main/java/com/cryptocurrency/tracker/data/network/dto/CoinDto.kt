package com.cryptocurrency.tracker.data.network.dto

import com.cryptocurrency.tracker.core.database.CoinEntity
import com.cryptocurrency.tracker.domain.model.Coin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("image") val imageUrl: String,
    @SerialName("current_price") val currentPrice: Double,
    @SerialName("price_change_percentage_24h") val priceChange24h: Double
) {
    fun toCoin(): Coin {
        return Coin(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            priceUsd = currentPrice,
            changePercent24Hr = priceChange24h
        )
    }

    fun toCoinEntity(): CoinEntity {
        return CoinEntity(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            priceUsd = currentPrice,
            changePercent24Hr = priceChange24h
        )
    }
}
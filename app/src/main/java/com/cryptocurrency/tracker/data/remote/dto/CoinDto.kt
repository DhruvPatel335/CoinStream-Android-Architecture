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
    @SerialName("price_change_percentage_24h") val priceChange24h: Double? = 0.0,
    @SerialName("market_cap") val marketCap: Double? = 0.0,
    @SerialName("market_cap_rank") val marketCapRank: Int? = 0,
    @SerialName("total_volume") val totalVolume: Double? = 0.0,
    @SerialName("high_24h") val high24h: Double? = 0.0,
    @SerialName("low_24h") val low24h: Double? = 0.0,
    @SerialName("ath") val ath: Double? = 0.0,
    @SerialName("atl") val atl: Double? = 0.0,
    @SerialName("sparkline_in_7d") val sparklineIn7d: SparklineDto? = null
) {
    fun toCoin(): Coin {
        return Coin(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            priceUsd = currentPrice ?: 0.0,
            changePercent24Hr = priceChange24h ?: 0.0,
            marketCap = marketCap ?: 0.0,
            marketCapRank = marketCapRank ?: 0,
            totalVolume = totalVolume ?: 0.0,
            high24h = high24h ?: 0.0,
            low24h = low24h ?: 0.0,
            ath = ath ?: 0.0,
            atl = atl ?: 0.0,
            sparkline = sparklineIn7d?.price ?: emptyList()
        )
    }

    fun toCoinEntity(): CoinEntity {
        return CoinEntity(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            priceUsd = currentPrice ?: 0.0,
            changePercent24Hr = priceChange24h ?: 0.0,
            marketCap = marketCap ?: 0.0,
            marketCapRank = marketCapRank ?: 0,
            totalVolume = totalVolume ?: 0.0,
            high24h = high24h ?: 0.0,
            low24h = low24h ?: 0.0,
            ath = ath ?: 0.0,
            atl = atl ?: 0.0,
            sparkline = sparklineIn7d?.price ?: emptyList()
        )
    }
}

@Serializable
data class SparklineDto(
    @SerialName("price") val price: List<Double>? = emptyList()
)
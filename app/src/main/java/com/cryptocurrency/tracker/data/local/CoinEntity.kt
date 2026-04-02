package com.cryptocurrency.tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cryptocurrency.tracker.domain.model.Coin

@Entity(tableName = "coins")
data class CoinEntity(
    @PrimaryKey val id: String,
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
) {
    fun toCoin(): Coin {
        return Coin(
            id = id,
            name = name,
            symbol = symbol,
            imageUrl = imageUrl,
            priceUsd = priceUsd,
            changePercent24Hr = changePercent24Hr,
            marketCap = marketCap,
            marketCapRank = marketCapRank,
            totalVolume = totalVolume,
            high24h = high24h,
            low24h = low24h,
            ath = ath,
            atl = atl,
            sparkline = sparkline
        )
    }
}

fun Coin.toEntity(): CoinEntity {
    return CoinEntity(
        id = id,
        name = name,
        symbol = symbol,
        imageUrl = imageUrl,
        priceUsd = priceUsd,
        changePercent24Hr = changePercent24Hr,
        marketCap = marketCap,
        marketCapRank = marketCapRank,
        totalVolume = totalVolume,
        high24h = high24h,
        low24h = low24h,
        ath = ath,
        atl = atl,
        sparkline = sparkline
    )
}
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
    val changePercent24Hr: Double
) {
    fun toCoin(): Coin {
        return Coin(
            id = id,
            name = name,
            symbol = symbol,
            imageUrl = imageUrl,
            priceUsd = priceUsd,
            changePercent24Hr = changePercent24Hr
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
        changePercent24Hr = changePercent24Hr
    )
}
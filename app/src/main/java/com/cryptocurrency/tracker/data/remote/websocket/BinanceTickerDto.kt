package com.cryptocurrency.tracker.data.remote.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceTickerDto(
    @SerialName("s") val symbol: String, // Symbol e.g. BTCUSDT
    @SerialName("c") val price: String,  // Last price
    @SerialName("P") val priceChangePercent: String // Price change percent
)

package com.cryptocurrency.tracker.domain.use_case

import com.cryptocurrency.tracker.data.local.CoinDao
import javax.inject.Inject

class UpdateCoinPriceUseCase @Inject constructor(
    private val dao: CoinDao
) {
    suspend operator fun invoke(symbol: String, price: Double, changePercent: Double, lastUpdate: Long) {
        dao.updateCoinPrice(symbol, price, changePercent, lastUpdate)
    }
}

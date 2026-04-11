package com.cryptocurrency.tracker.domain.use_case

import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import javax.inject.Inject

class GetCoinUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(id: String): Coin? {
        return repository.getCoinById(id)
    }
}
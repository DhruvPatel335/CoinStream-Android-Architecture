package com.cryptocurrency.tracker.domain.use_case

import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCoinsUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    operator fun invoke(): Flow<List<Coin>> {
        return repository.observeCoins()
    }
}

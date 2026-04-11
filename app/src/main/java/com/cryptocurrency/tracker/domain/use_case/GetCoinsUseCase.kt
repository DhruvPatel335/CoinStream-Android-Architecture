package com.cryptocurrency.tracker.domain.use_case

import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoinsUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    operator fun invoke(): Flow<Resource<List<Coin>>> {
        return repository.getCoins()
    }
}
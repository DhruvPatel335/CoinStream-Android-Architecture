package com.cryptocurrency.tracker.domain.use_case

import androidx.paging.PagingData
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCoinsPagedUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    operator fun invoke(): Flow<PagingData<Coin>> {
        return repository.observeCoinsPaged()
    }
}

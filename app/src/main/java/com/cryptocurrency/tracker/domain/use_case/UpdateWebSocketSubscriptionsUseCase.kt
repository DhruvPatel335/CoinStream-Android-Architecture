package com.cryptocurrency.tracker.domain.use_case

import com.cryptocurrency.tracker.domain.repository.CoinRepository
import javax.inject.Inject

class UpdateWebSocketSubscriptionsUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(symbols: List<String>) {
        repository.updateWebSocketSubscriptions(symbols)
    }
}

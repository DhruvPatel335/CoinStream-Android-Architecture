package com.cryptocurrency.tracker.data.repository

import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CoinRepositoryImpl : CoinRepository {
    override fun getCoins(): Flow<List<Coin>> {
        return flow { emit(emptyList()) }
    }

    override suspend fun refreshCoins() {
        // Implementation
    }
}
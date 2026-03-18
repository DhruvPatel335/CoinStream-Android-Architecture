package com.cryptocurrency.tracker.domain.repository

import com.cryptocurrency.tracker.domain.model.Coin
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getCoins(): Flow<List<Coin>>
    suspend fun refreshCoins()
}
package com.cryptocurrency.tracker.domain.repository

import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.model.Coin
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getCoins(): Flow<Resource<List<Coin>>>
    suspend fun getCoinById(id: String): Coin?
}
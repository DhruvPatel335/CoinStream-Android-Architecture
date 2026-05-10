package com.cryptocurrency.tracker.domain.repository

import androidx.paging.PagingData
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.model.Coin
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getCoins(): Flow<Resource<List<Coin>>>
    fun observeCoins(): Flow<List<Coin>>
    fun observeCoinsPaged(): Flow<PagingData<Coin>>
    suspend fun updateWebSocketSubscriptions(symbols: List<String>)
    suspend fun getCoinById(id: String): Coin?
}
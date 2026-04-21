package com.cryptocurrency.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    @Query("SELECT * FROM coins")
    fun observeAllCoins(): Flow<List<CoinEntity>>

    @Query("SELECT * FROM coins")
    suspend fun getAllCoins(): List<CoinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoins(coins: List<CoinEntity>)

    @Query("DELETE FROM coins")
    suspend fun deleteCoins()

    @Query("SELECT * FROM coins WHERE id = :id")
    suspend fun getCoinById(id: String): CoinEntity?

    @Query("UPDATE coins SET priceUsd = :price, changePercent24Hr = :changePercent WHERE symbol = :symbol")
    suspend fun updateCoinPrice(symbol: String, price: Double, changePercent: Double)
}
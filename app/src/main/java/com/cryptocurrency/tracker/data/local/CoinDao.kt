package com.cryptocurrency.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CoinDao {
    @Query("SELECT * FROM coins")
    suspend fun getAllCoins(): List<CoinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoins(coins: List<CoinEntity>)

    @Query("DELETE FROM coins")
    suspend fun deleteCoins()

    @Query("SELECT * FROM coins WHERE id = :id")
    suspend fun getCoinById(id: String): CoinEntity?
}
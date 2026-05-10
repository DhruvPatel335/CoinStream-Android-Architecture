package com.cryptocurrency.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CoinRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<CoinRemoteKey>)

    @Query("SELECT * FROM coin_remote_keys WHERE coinId = :coinId")
    suspend fun remoteKeysCoinId(coinId: String): CoinRemoteKey?

    @Query("DELETE FROM coin_remote_keys")
    suspend fun clearRemoteKeys()
}
package com.cryptocurrency.tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_remote_keys")
data class CoinRemoteKey(
    @PrimaryKey val coinId: String,
    val prevKey: Int?,
    val nextKey: Int?,
)
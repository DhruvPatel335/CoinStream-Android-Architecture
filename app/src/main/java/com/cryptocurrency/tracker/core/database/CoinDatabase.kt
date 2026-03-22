package com.cryptocurrency.tracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CoinEntity::class], version = 1)
abstract class CoinDatabase : RoomDatabase() {
    abstract val coinDao: CoinDao
}
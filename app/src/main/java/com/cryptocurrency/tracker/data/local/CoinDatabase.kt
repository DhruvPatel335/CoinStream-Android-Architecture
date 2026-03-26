package com.cryptocurrency.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CoinEntity::class], version = 2, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class CoinDatabase : RoomDatabase() {
    abstract val coinDao: CoinDao
}
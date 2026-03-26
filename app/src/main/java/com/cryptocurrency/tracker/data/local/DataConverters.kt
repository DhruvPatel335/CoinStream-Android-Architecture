package com.cryptocurrency.tracker.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataConverters {
    @TypeConverter
    fun fromList(value: List<Double>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<Double> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
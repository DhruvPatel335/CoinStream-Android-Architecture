package com.cryptocurrency.tracker.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cryptocurrency.tracker.data.local.CoinDao
import com.cryptocurrency.tracker.data.local.CoinDatabase
import com.cryptocurrency.tracker.data.remote.ApiService
import com.cryptocurrency.tracker.data.repository.CoinRepositoryImpl
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient, json: Json): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE coins ADD COLUMN sparkline TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE coins ADD COLUMN marketCap REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE coins ADD COLUMN marketCapRank INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE coins ADD COLUMN totalVolume REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE coins ADD COLUMN high24h REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE coins ADD COLUMN low24h REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE coins ADD COLUMN ath REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE coins ADD COLUMN atl REAL NOT NULL DEFAULT 0.0")
        }
    }

    @Provides
    @Singleton
    fun provideCoinDatabase(@ApplicationContext context: Context): CoinDatabase {
        return Room.databaseBuilder(
            context,
            CoinDatabase::class.java,
            "coin_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }

    @Provides
    @Singleton
    fun provideCoinDao(database: CoinDatabase): CoinDao {
        return database.coinDao
    }

    @Provides
    @Singleton
    fun provideCoinRepository(
        api: ApiService,
        dao: CoinDao
    ): CoinRepository {
        return CoinRepositoryImpl(api, dao)
    }
}
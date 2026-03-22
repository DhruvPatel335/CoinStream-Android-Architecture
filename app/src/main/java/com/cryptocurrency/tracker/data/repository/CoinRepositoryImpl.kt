package com.cryptocurrency.tracker.data.repository

import com.cryptocurrency.tracker.core.database.CoinDao
import com.cryptocurrency.tracker.core.database.toEntity
import com.cryptocurrency.tracker.core.network.ApiService
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

class CoinRepositoryImpl(
    private val api: ApiService,
    private val dao: CoinDao
) : CoinRepository {

    override fun getCoins(): Flow<Resource<List<Coin>>> = flow {
        emit(Resource.Loading())

        // 1. Try to get cached data from Room first
        val localCoins = dao.getAllCoins().map { it.toCoin() }
        emit(Resource.Loading(data = localCoins))

        try {
            // 2. Fetch fresh data from Network
            val remoteCoins = api.getCoins()

            // 3. Update the Local Database (Delete old, Insert new)
            dao.deleteCoins()
            dao.insertCoins(remoteCoins.map { it.toCoinEntity()})
            
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = "Oops, something went wrong!",
                data = localCoins
            ))
        } catch (e: IOException) {
            emit(Resource.Error(
                message = "Couldn't reach server. Check your internet connection.",
                data = localCoins
            ))
        }

        // 4. Emit the final updated list from the Database
        val updatedLocalCoins = dao.getAllCoins().map { it.toCoin() }
        emit(Resource.Success(updatedLocalCoins))
    }

    override suspend fun getCoinById(id: String): Coin? {
        return dao.getCoinById(id)?.toCoin()
    }
}
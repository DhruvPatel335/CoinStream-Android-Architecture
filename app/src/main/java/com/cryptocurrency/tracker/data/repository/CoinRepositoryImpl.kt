package com.cryptocurrency.tracker.data.repository

import com.cryptocurrency.tracker.data.local.CoinDao
import com.cryptocurrency.tracker.data.remote.ApiService
import com.cryptocurrency.tracker.core.util.Resource
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class CoinRepositoryImpl(
    private val api: ApiService,
    private val dao: CoinDao
) : CoinRepository {

    override fun getCoins(): Flow<Resource<List<Coin>>> = flow {
        emit(Resource.Loading())

        val localCoins = dao.getAllCoins().map { it.toCoin() }
        emit(Resource.Loading(data = localCoins))

        try {
            val remoteCoins = api.getCoins()
            
            dao.deleteCoins()
            dao.insertCoins(remoteCoins.map { it.toCoinEntity() })
            
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

        val updatedLocalCoins = dao.getAllCoins().map { it.toCoin() }
        emit(Resource.Success(updatedLocalCoins))
    }

    override fun observeCoins(): Flow<List<Coin>> {
        return dao.observeAllCoins().map { entities ->
            entities.map { it.toCoin() }
        }
    }

    override suspend fun getCoinById(id: String): Coin? {
        return dao.getCoinById(id)?.toCoin()
    }
}

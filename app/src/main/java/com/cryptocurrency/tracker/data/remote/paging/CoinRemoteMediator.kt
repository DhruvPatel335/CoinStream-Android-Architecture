package com.cryptocurrency.tracker.data.remote.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.cryptocurrency.tracker.data.local.CoinDatabase
import com.cryptocurrency.tracker.data.local.CoinEntity
import com.cryptocurrency.tracker.data.local.CoinRemoteKey
import com.cryptocurrency.tracker.data.remote.ApiService
import java.io.IOException
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class CoinRemoteMediator(
    private val coinDb: CoinDatabase,
    private val coinApi: ApiService
) : RemoteMediator<Int, CoinEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CoinEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val apiResponse = coinApi.getCoins(
                page = page,
                perPage = state.config.pageSize
            )

            val endOfPaginationReached = apiResponse.isEmpty()
            coinDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    coinDb.coinRemoteKeyDao.clearRemoteKeys()
                    coinDb.coinDao.deleteCoins()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = apiResponse.map {
                    CoinRemoteKey(coinId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                coinDb.coinRemoteKeyDao.insertAll(keys)
                coinDb.coinDao.insertCoins(apiResponse.map { it.toCoinEntity() })
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, CoinEntity>): CoinRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { coin ->
                coinDb.coinRemoteKeyDao.remoteKeysCoinId(coin.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, CoinEntity>): CoinRemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { coin ->
                coinDb.coinRemoteKeyDao.remoteKeysCoinId(coin.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, CoinEntity>
    ): CoinRemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { coinId ->
                coinDb.coinRemoteKeyDao.remoteKeysCoinId(coinId)
            }
        }
    }
}
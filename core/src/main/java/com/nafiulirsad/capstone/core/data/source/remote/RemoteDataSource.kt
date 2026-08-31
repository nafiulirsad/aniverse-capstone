package com.nafiulirsad.capstone.core.data.source.remote

import android.util.Log
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiResponse
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiService
import com.nafiulirsad.capstone.core.data.source.remote.network.NetworkErrorMapper
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeDetailResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteDataSource(private val apiService: ApiService, private val errorMapper: NetworkErrorMapper) {

    fun getTopAnime(): Flow<ApiResponse<AnimeListResponse>> = listCall {
        apiService.getTopAnime(limit = PAGE_SIZE, sort = SORT_MOST_POPULAR)
    }

    fun searchAnime(query: String): Flow<ApiResponse<AnimeListResponse>> = listCall {
        apiService.searchAnime(query = query, limit = PAGE_SIZE)
    }

    fun getAnimeDetail(animeId: Int): Flow<ApiResponse<AnimeDetailResponse>> = flow {
        try {
            val response = apiService.getAnimeDetail(animeId, include = INCLUDE_CATEGORIES)
            emit(if (response.data == null) ApiResponse.Empty else ApiResponse.Success(response))
        } catch (throwable: Exception) {
            Log.e(TAG, "getAnimeDetail($animeId) failed", throwable)
            emit(ApiResponse.Error(errorMapper.map(throwable)))
        }
    }.flowOn(Dispatchers.IO)

    private fun listCall(
        request: suspend () -> AnimeListResponse,
    ): Flow<ApiResponse<AnimeListResponse>> = flow {
        try {
            val response = request()
            emit(
                if (response.data.isNullOrEmpty()) {
                    ApiResponse.Empty
                } else {
                    ApiResponse.Success(response)
                },
            )
        } catch (throwable: Exception) {
            Log.e(TAG, "Network request failed", throwable)
            emit(ApiResponse.Error(errorMapper.map(throwable)))
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val BASE_URL = "https://kitsu.io/api/edge/"

        private const val TAG = "RemoteDataSource"
        private const val PAGE_SIZE = 20
        private const val SORT_MOST_POPULAR = "-userCount"
        private const val INCLUDE_CATEGORIES = "categories"
    }
}

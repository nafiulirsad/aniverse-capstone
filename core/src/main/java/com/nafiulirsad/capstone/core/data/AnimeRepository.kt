package com.nafiulirsad.capstone.core.data

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.data.mapper.toDomain
import com.nafiulirsad.capstone.core.data.mapper.toDomainList
import com.nafiulirsad.capstone.core.data.mapper.toDomainOrNull
import com.nafiulirsad.capstone.core.data.mapper.toEntities
import com.nafiulirsad.capstone.core.data.mapper.toFavoriteEntity
import com.nafiulirsad.capstone.core.data.source.local.LocalDataSource
import com.nafiulirsad.capstone.core.data.source.remote.RemoteDataSource
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiResponse
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.repository.IAnimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AnimeRepository(private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource) :
    IAnimeRepository {

    override fun getTopAnime(forceRefresh: Boolean): Flow<Resource<List<Anime>>> =
        networkBoundResource(
            query = { localDataSource.getTopAnime().map { cached -> cached.map { it.toDomain() } } },
            fetch = { remoteDataSource.getTopAnime() },
            saveFetchResult = { response -> localDataSource.replaceTopAnime(response.toEntities()) },
            shouldFetch = { cached -> forceRefresh || cached.isEmpty() },
        )

    override fun searchAnime(query: String): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading())
        when (val response = remoteDataSource.searchAnime(query).first()) {
            is ApiResponse.Success -> {
                emit(Resource.Success(response.data.toDomainList()))
            }

            is ApiResponse.Empty -> {
                emit(Resource.Success(emptyList()))
            }

            // Offline or a server hiccup still gets an answer: whatever the cache can match.
            is ApiResponse.Error -> {
                val cached = localDataSource.searchCachedAnime(query).first().map { it.toDomain() }
                emit(Resource.Error(response.message, cached.ifEmpty { null }))
            }
        }
    }

    override fun getAnimeDetail(animeId: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading())

        val cached = readCached(animeId)
        if (cached != null) emit(Resource.Loading(cached))

        when (val response = remoteDataSource.getAnimeDetail(animeId).first()) {
            is ApiResponse.Success -> {
                val anime = response.data.toDomainOrNull()
                when {
                    anime != null -> {
                        refreshFavoriteSnapshot(anime)
                        emit(Resource.Success(anime))
                    }

                    cached != null -> {
                        emit(Resource.Success(cached))
                    }

                    else -> {
                        emit(Resource.Error(EMPTY_DETAIL_MESSAGE))
                    }
                }
            }

            is ApiResponse.Empty -> {
                if (cached != null) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error(EMPTY_DETAIL_MESSAGE))
                }
            }

            is ApiResponse.Error -> {
                emit(Resource.Error(response.message, cached))
            }
        }
    }

    override fun getFavoriteAnime(): Flow<List<Anime>> =
        localDataSource.getFavoriteAnime().map { favorites -> favorites.map { it.toDomain() } }

    override fun isFavorite(animeId: Int): Flow<Boolean> = localDataSource.isFavorite(animeId)

    override suspend fun setFavorite(anime: Anime, favorite: Boolean) {
        if (favorite) {
            localDataSource.insertFavorite(anime.toFavoriteEntity(System.currentTimeMillis()))
        } else {
            localDataSource.deleteFavorite(anime.animeId)
        }
    }

    /** Detail works offline for anything already stored, favorites first. */
    private suspend fun readCached(animeId: Int): Anime? =
        localDataSource.getFavoriteAnime(animeId).first()?.toDomain()
            ?: localDataSource.getCachedAnime(animeId).first()?.toDomain()

    /** Keeps an already-favorited item in sync with the freshest payload from the network. */
    private suspend fun refreshFavoriteSnapshot(anime: Anime) {
        val stored = localDataSource.getFavoriteAnime(anime.animeId).first() ?: return
        localDataSource.insertFavorite(anime.toFavoriteEntity(stored.favoritedAt))
    }

    private companion object {
        const val EMPTY_DETAIL_MESSAGE = "Detail anime tidak ditemukan."
    }
}

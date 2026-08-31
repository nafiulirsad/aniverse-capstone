package com.nafiulirsad.capstone.core.domain.repository

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import kotlinx.coroutines.flow.Flow

/**
 * The contract is owned by the domain layer and implemented by the data layer,
 * so the dependency arrow points inwards (dependency inversion).
 */
interface IAnimeRepository {
    fun getTopAnime(forceRefresh: Boolean): Flow<Resource<List<Anime>>>

    fun searchAnime(query: String): Flow<Resource<List<Anime>>>

    fun getAnimeDetail(animeId: Int): Flow<Resource<Anime>>

    fun getFavoriteAnime(): Flow<List<Anime>>

    fun isFavorite(animeId: Int): Flow<Boolean>

    suspend fun setFavorite(anime: Anime, favorite: Boolean)
}

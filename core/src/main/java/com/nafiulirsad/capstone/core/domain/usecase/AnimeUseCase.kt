package com.nafiulirsad.capstone.core.domain.usecase

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import kotlinx.coroutines.flow.Flow

interface AnimeUseCase {
    fun getTopAnime(forceRefresh: Boolean): Flow<Resource<List<Anime>>>

    fun searchAnime(query: String): Flow<Resource<List<Anime>>>

    fun getAnimeDetail(animeId: Int): Flow<Resource<Anime>>

    fun getFavoriteAnime(): Flow<List<Anime>>

    fun isFavorite(animeId: Int): Flow<Boolean>

    suspend fun setFavorite(anime: Anime, favorite: Boolean)
}

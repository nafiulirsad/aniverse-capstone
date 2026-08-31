package com.nafiulirsad.capstone.core.domain.usecase

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.repository.IAnimeRepository
import kotlinx.coroutines.flow.Flow

class AnimeInteractor(private val animeRepository: IAnimeRepository) : AnimeUseCase {

    override fun getTopAnime(forceRefresh: Boolean): Flow<Resource<List<Anime>>> =
        animeRepository.getTopAnime(forceRefresh)

    override fun searchAnime(query: String): Flow<Resource<List<Anime>>> =
        animeRepository.searchAnime(query)

    override fun getAnimeDetail(animeId: Int): Flow<Resource<Anime>> =
        animeRepository.getAnimeDetail(animeId)

    override fun getFavoriteAnime(): Flow<List<Anime>> = animeRepository.getFavoriteAnime()

    override fun isFavorite(animeId: Int): Flow<Boolean> = animeRepository.isFavorite(animeId)

    override suspend fun setFavorite(anime: Anime, favorite: Boolean) =
        animeRepository.setFavorite(anime, favorite)
}

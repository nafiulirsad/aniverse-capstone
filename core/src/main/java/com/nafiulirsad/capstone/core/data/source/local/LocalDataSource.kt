package com.nafiulirsad.capstone.core.data.source.local

import com.nafiulirsad.capstone.core.data.source.local.entity.AnimeEntity
import com.nafiulirsad.capstone.core.data.source.local.entity.FavoriteAnimeEntity
import com.nafiulirsad.capstone.core.data.source.local.room.AnimeDao
import kotlinx.coroutines.flow.Flow

class LocalDataSource(private val animeDao: AnimeDao) {

    fun getTopAnime(): Flow<List<AnimeEntity>> = animeDao.getTopAnime()

    fun getCachedAnime(animeId: Int): Flow<AnimeEntity?> = animeDao.getCachedAnime(animeId)

    fun searchCachedAnime(query: String): Flow<List<AnimeEntity>> =
        animeDao.searchCachedAnime(query)

    suspend fun replaceTopAnime(anime: List<AnimeEntity>) = animeDao.replaceTopAnime(anime)

    fun getFavoriteAnime(): Flow<List<FavoriteAnimeEntity>> = animeDao.getFavoriteAnime()

    fun getFavoriteAnime(animeId: Int): Flow<FavoriteAnimeEntity?> =
        animeDao.getFavoriteAnime(animeId)

    fun isFavorite(animeId: Int): Flow<Boolean> = animeDao.isFavorite(animeId)

    suspend fun insertFavorite(anime: FavoriteAnimeEntity) = animeDao.insertFavorite(anime)

    suspend fun deleteFavorite(animeId: Int) = animeDao.deleteFavorite(animeId)
}

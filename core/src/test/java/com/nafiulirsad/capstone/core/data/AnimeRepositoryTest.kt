package com.nafiulirsad.capstone.core.data

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.data.source.local.LocalDataSource
import com.nafiulirsad.capstone.core.data.source.local.entity.AnimeEntity
import com.nafiulirsad.capstone.core.data.source.local.entity.FavoriteAnimeEntity
import com.nafiulirsad.capstone.core.data.source.remote.RemoteDataSource
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiResponse
import com.nafiulirsad.capstone.core.domain.model.Anime
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeRepositoryTest {

    private val remoteDataSource: RemoteDataSource = mockk()
    private val localDataSource: LocalDataSource = mockk()
    private val repository = AnimeRepository(remoteDataSource, localDataSource)

    @Test
    fun `a failed search falls back to whatever the cache can match`() = runTest {
        every { remoteDataSource.searchAnime(QUERY) } returns
            flowOf(ApiResponse.Error(ERROR_MESSAGE))
        every { localDataSource.searchCachedAnime(QUERY) } returns flowOf(listOf(animeEntity))

        val result = repository.searchAnime(QUERY).drop(1).first()

        assertTrue(result is Resource.Error)
        assertEquals(ERROR_MESSAGE, result.message)
        assertEquals(listOf(ANIME_TITLE), result.data?.map(Anime::title))
    }

    @Test
    fun `a failed search with an empty cache reports the error without stale data`() = runTest {
        every { remoteDataSource.searchAnime(QUERY) } returns
            flowOf(ApiResponse.Error(ERROR_MESSAGE))
        every { localDataSource.searchCachedAnime(QUERY) } returns flowOf(emptyList())

        val result = repository.searchAnime(QUERY).drop(1).first()

        assertTrue(result is Resource.Error)
        assertNull(result.data)
    }

    @Test
    fun `favorites are read from the database and handed over as domain models`() = runTest {
        every { localDataSource.getFavoriteAnime() } returns flowOf(listOf(favoriteEntity))

        val favorites = repository.getFavoriteAnime().first()

        assertEquals(listOf(ANIME_ID), favorites.map(Anime::animeId))
    }

    @Test
    fun `favoriting stores the anime, unfavoriting removes it again`() = runTest {
        coEvery { localDataSource.insertFavorite(any()) } just Runs
        coEvery { localDataSource.deleteFavorite(ANIME_ID) } just Runs

        repository.setFavorite(anime, favorite = true)
        repository.setFavorite(anime, favorite = false)

        coVerify(exactly = 1) { localDataSource.insertFavorite(match { it.animeId == ANIME_ID }) }
        coVerify(exactly = 1) { localDataSource.deleteFavorite(ANIME_ID) }
    }

    private companion object {
        const val ANIME_ID = 7442
        const val ANIME_TITLE = "Attack on Titan"
        const val QUERY = "titan"
        const val ERROR_MESSAGE = "Tidak ada koneksi internet."

        val animeEntity = AnimeEntity(
            animeId = ANIME_ID,
            title = ANIME_TITLE,
            englishTitle = ANIME_TITLE,
            imageUrl = "https://media.kitsu.app/poster.jpg",
            largeImageUrl = "https://media.kitsu.app/poster-large.jpg",
            type = "TV",
            episodes = 25,
            status = "finished",
            score = 8.4,
            ranking = 48,
            members = 100_000,
            year = 2013,
            episodeMinutes = 24,
            ageRating = "R",
            synopsis = "Sinopsis.",
            genres = listOf("Action"),
            trailerUrl = null,
        )

        val favoriteEntity = FavoriteAnimeEntity(
            animeId = ANIME_ID,
            title = ANIME_TITLE,
            englishTitle = ANIME_TITLE,
            imageUrl = animeEntity.imageUrl,
            largeImageUrl = animeEntity.largeImageUrl,
            type = animeEntity.type,
            episodes = animeEntity.episodes,
            status = animeEntity.status,
            score = animeEntity.score,
            ranking = animeEntity.ranking,
            members = animeEntity.members,
            year = animeEntity.year,
            episodeMinutes = animeEntity.episodeMinutes,
            ageRating = animeEntity.ageRating,
            synopsis = animeEntity.synopsis,
            genres = animeEntity.genres,
            trailerUrl = null,
            favoritedAt = 1_700_000_000_000L,
        )

        val anime = Anime(
            animeId = ANIME_ID,
            title = ANIME_TITLE,
            englishTitle = ANIME_TITLE,
            imageUrl = animeEntity.imageUrl,
            largeImageUrl = animeEntity.largeImageUrl,
            type = animeEntity.type,
            episodes = animeEntity.episodes,
            status = animeEntity.status,
            score = animeEntity.score,
            ranking = animeEntity.ranking,
            members = animeEntity.members,
            year = animeEntity.year,
            episodeMinutes = animeEntity.episodeMinutes,
            ageRating = animeEntity.ageRating,
            synopsis = animeEntity.synopsis,
            genres = animeEntity.genres,
            trailerUrl = null,
        )
    }
}

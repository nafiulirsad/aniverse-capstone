package com.nafiulirsad.capstone.core.domain.usecase

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.repository.IAnimeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The interactor is the boundary the presentation layer talks to. These tests pin down that it
 * forwards to the repository untouched - no hidden caching, no swallowed arguments.
 */
class AnimeInteractorTest {

    private val repository: IAnimeRepository = mockk()
    private val interactor: AnimeUseCase = AnimeInteractor(repository)

    @Test
    fun `the refresh flag reaches the repository as given`() = runTest {
        every { repository.getTopAnime(true) } returns flowOf(Resource.Success(listOf(anime)))

        val result = interactor.getTopAnime(forceRefresh = true).first()

        assertTrue(result is Resource.Success)
        assertEquals(listOf(anime), result.data)
        verify(exactly = 1) { repository.getTopAnime(true) }
    }

    @Test
    fun `the favorite state stream is passed through`() = runTest {
        every { repository.isFavorite(anime.animeId) } returns flowOf(true)

        assertTrue(interactor.isFavorite(anime.animeId).first())
    }

    @Test
    fun `setting a favorite is delegated with both arguments intact`() = runTest {
        coEvery { repository.setFavorite(anime, true) } just Runs

        interactor.setFavorite(anime, favorite = true)

        coVerify(exactly = 1) { repository.setFavorite(anime, true) }
    }

    private companion object {
        val anime = Anime(
            animeId = 1,
            title = "Cowboy Bebop",
            englishTitle = "Cowboy Bebop",
            imageUrl = "https://media.kitsu.app/poster.jpg",
            largeImageUrl = "https://media.kitsu.app/poster-large.jpg",
            type = "TV",
            episodes = 26,
            status = "finished",
            score = 8.2,
            ranking = 12,
            members = 90_000,
            year = 1998,
            episodeMinutes = 24,
            ageRating = "R",
            synopsis = "Sinopsis.",
            genres = listOf("Action", "Sci-Fi"),
            trailerUrl = "https://www.youtube.com/watch?v=abc",
        )
    }
}

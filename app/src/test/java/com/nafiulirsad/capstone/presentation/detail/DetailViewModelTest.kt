package com.nafiulirsad.capstone.presentation.detail

import app.cash.turbine.test
import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.usecase.AnimeUseCase
import com.nafiulirsad.capstone.presentation.mapper.AnimeUiMapper
import com.nafiulirsad.capstone.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val animeUseCase: AnimeUseCase = mockk()
    private val animeUiMapper: AnimeUiMapper = mockk {
        every { toAnimeDetailUi(any()) } answers {
            mockk(relaxed = true) { every { title } returns firstArg<Anime>().title }
        }
    }

    @Test
    fun `detail data and favorite state are merged into a single state`() = runTest {
        every { animeUseCase.getAnimeDetail(ANIME_ID) } returns
            flowOf(Resource.Success(anime))
        every { animeUseCase.isFavorite(ANIME_ID) } returns flowOf(true)

        val viewModel = DetailViewModel(animeUseCase, animeUiMapper, ANIME_ID)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ANIME_TITLE, state.anime?.title)
            assertTrue(state.isFavorite)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `an error without any cached anime blocks the screen`() = runTest {
        every { animeUseCase.getAnimeDetail(ANIME_ID) } returns flowOf(Resource.Error(ERROR))
        every { animeUseCase.isFavorite(ANIME_ID) } returns flowOf(false)

        val viewModel = DetailViewModel(animeUseCase, animeUiMapper, ANIME_ID)

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.anime)
            assertTrue(state.isBlockingError)
        }
    }

    @Test
    fun `the toggle stores the anime and reports the new state exactly once`() = runTest {
        every { animeUseCase.getAnimeDetail(ANIME_ID) } returns flowOf(Resource.Success(anime))
        every { animeUseCase.isFavorite(ANIME_ID) } returns flowOf(false)
        coEvery { animeUseCase.setFavorite(anime, true) } just Runs

        val viewModel = DetailViewModel(animeUseCase, animeUiMapper, ANIME_ID)
        viewModel.uiState.test { awaitItem() }

        viewModel.favoriteEvent.test {
            viewModel.toggleFavorite()

            assertTrue(awaitItem())
        }
        coVerify(exactly = 1) { animeUseCase.setFavorite(anime, true) }
    }

    @Test
    fun `the toggle does nothing while the anime has not loaded yet`() = runTest {
        every { animeUseCase.getAnimeDetail(ANIME_ID) } returns flowOf(Resource.Loading())
        every { animeUseCase.isFavorite(ANIME_ID) } returns flowOf(false)

        val viewModel = DetailViewModel(animeUseCase, animeUiMapper, ANIME_ID)
        viewModel.toggleFavorite()

        coVerify(exactly = 0) { animeUseCase.setFavorite(any(), any()) }
    }

    private companion object {
        const val ANIME_ID = 1
        const val ANIME_TITLE = "Cowboy Bebop"
        const val ERROR = "Detail anime tidak ditemukan."

        val anime = Anime(
            animeId = ANIME_ID,
            title = ANIME_TITLE,
            englishTitle = ANIME_TITLE,
            imageUrl = "https://media.kitsu.app/poster.jpg",
            largeImageUrl = "https://media.kitsu.app/poster-large.jpg",
            type = "TV",
            episodes = 26,
            status = "finished",
            score = 8.42,
            ranking = 12,
            members = 90_000,
            year = 1998,
            episodeMinutes = 24,
            ageRating = "R",
            synopsis = "Sinopsis.",
            genres = listOf("Action"),
            trailerUrl = null,
        )
    }
}

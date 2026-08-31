package com.nafiulirsad.capstone.presentation.home

import app.cash.turbine.test
import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.usecase.AnimeUseCase
import com.nafiulirsad.capstone.presentation.mapper.AnimeUiMapper
import com.nafiulirsad.capstone.presentation.model.AnimeUi
import com.nafiulirsad.capstone.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val animeUseCase: AnimeUseCase = mockk()
    private val animeUiMapper: AnimeUiMapper = mockk {
        every { toAnimeUi(any()) } answers { firstArg<Anime>().toUi() }
    }

    @Test
    fun `the top list is shown as soon as the use case answers`() = runTest {
        every { animeUseCase.getTopAnime(any()) } returns flowOf(Resource.Success(listOf(anime)))

        val viewModel = HomeViewModel(animeUseCase, animeUiMapper)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(ANIME_TITLE), state.items.map(AnimeUi::title))
            assertFalse(state.isLoading)
            assertFalse(state.isSearching)
        }
    }

    @Test
    fun `typing switches the screen over to the search use case after the debounce`() = runTest {
        every { animeUseCase.getTopAnime(any()) } returns flowOf(Resource.Success(listOf(anime)))
        every { animeUseCase.searchAnime(QUERY) } returns flowOf(Resource.Success(emptyList()))

        val viewModel = HomeViewModel(animeUseCase, animeUiMapper)
        viewModel.uiState.test {
            awaitItem()

            viewModel.onQueryChanged(QUERY)
            advanceTimeBy(DEBOUNCE_WINDOW_MS)

            val state = awaitItem()
            assertTrue(state.isSearching)
            assertTrue(state.isEmpty)
        }
        verify(exactly = 1) { animeUseCase.searchAnime(QUERY) }
    }

    @Test
    fun `an error with cached items is reported as a non-blocking notice`() = runTest {
        every { animeUseCase.getTopAnime(any()) } returns
            flowOf(Resource.Error(ERROR_MESSAGE, listOf(anime)))

        val viewModel = HomeViewModel(animeUseCase, animeUiMapper)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ERROR_MESSAGE, state.errorMessage)
            assertFalse(state.isBlockingError)
        }
    }

    @Test
    fun `a pull to refresh asks the use case for fresh data`() = runTest {
        every { animeUseCase.getTopAnime(any()) } returns flowOf(Resource.Success(listOf(anime)))

        val viewModel = HomeViewModel(animeUseCase, animeUiMapper)
        viewModel.uiState.test {
            awaitItem()

            viewModel.onRefresh()
            advanceTimeBy(DEBOUNCE_WINDOW_MS)
            cancelAndIgnoreRemainingEvents()
        }

        verify { animeUseCase.getTopAnime(true) }
    }

    private fun Anime.toUi() = AnimeUi(
        animeId = animeId,
        title = title,
        posterUrl = imageUrl,
        scoreLabel = score.toString(),
        metaLabel = type.orEmpty(),
    )

    private companion object {
        const val ANIME_TITLE = "Cowboy Bebop"
        const val QUERY = "bebop"
        const val ERROR_MESSAGE = "Tidak ada koneksi internet."
        const val DEBOUNCE_WINDOW_MS = 500L

        val anime = Anime(
            animeId = 1,
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

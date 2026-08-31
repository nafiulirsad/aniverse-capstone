package com.nafiulirsad.capstone.presentation.mapper

import android.content.Context
import android.content.res.Resources
import com.nafiulirsad.capstone.R
import com.nafiulirsad.capstone.core.domain.model.Anime
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeUiMapperTest {

    private val resources: Resources = mockk {
        every { getQuantityString(R.plurals.format_episodes, EPISODES, EPISODES) } returns
            "$EPISODES eps"
        every { getQuantityString(R.plurals.format_duration, MINUTES, MINUTES) } returns
            "$MINUTES menit per episode"
    }

    private val context: Context = mockk {
        every { resources } returns this@AnimeUiMapperTest.resources
        every { getString(R.string.label_unknown) } returns UNKNOWN
        every { getString(R.string.label_no_synopsis) } returns NO_SYNOPSIS
        every { getString(R.string.status_finished) } returns FINISHED
        every { getString(R.string.status_airing) } returns AIRING
        every { getString(R.string.status_upcoming) } returns UPCOMING
        every { getString(R.string.format_rank, RANK) } returns "#$RANK"
    }

    private val mapper = AnimeUiMapper(context)

    @Test
    fun `a complete anime becomes display-ready strings`() {
        val ui = mapper.toAnimeUi(anime)

        assertEquals("8.42", ui.scoreLabel)
        assertEquals("TV • 26 eps • 1998", ui.metaLabel)
    }

    @Test
    fun `a missing score shows a placeholder instead of a crash`() {
        val ui = mapper.toAnimeUi(anime.copy(score = null))

        assertEquals("N/A", ui.scoreLabel)
    }

    @Test
    fun `the kitsu airing vocabulary is translated for the detail screen`() {
        assertEquals(FINISHED, mapper.toAnimeDetailUi(anime.copy(status = "finished")).statusLabel)
        assertEquals(AIRING, mapper.toAnimeDetailUi(anime.copy(status = "current")).statusLabel)
        assertEquals(UPCOMING, mapper.toAnimeDetailUi(anime.copy(status = "upcoming")).statusLabel)
        assertEquals(UNKNOWN, mapper.toAnimeDetailUi(anime.copy(status = "???")).statusLabel)
    }

    @Test
    fun `an english title equal to the canonical one is not repeated as a subtitle`() {
        val ui = mapper.toAnimeDetailUi(anime.copy(englishTitle = anime.title))

        assertTrue(ui.subtitle.isEmpty())
    }

    @Test
    fun `a large member count is abbreviated and a blank trailer is dropped`() {
        val ui = mapper.toAnimeDetailUi(anime.copy(members = 1_500_000, trailerUrl = "  "))

        assertEquals("1.5Jt", ui.membersLabel)
        assertNull(ui.trailerUrl)
    }

    @Test
    fun `a missing synopsis falls back to a readable sentence`() {
        val ui = mapper.toAnimeDetailUi(anime.copy(synopsis = null))

        assertEquals(NO_SYNOPSIS, ui.synopsis)
    }

    private companion object {
        const val RANK = 12
        const val EPISODES = 26
        const val MINUTES = 24

        const val UNKNOWN = "Tidak diketahui"
        const val NO_SYNOPSIS = "Sinopsis belum tersedia."
        const val FINISHED = "Selesai tayang"
        const val AIRING = "Sedang tayang"
        const val UPCOMING = "Akan tayang"

        val anime = Anime(
            animeId = 1,
            title = "Cowboy Bebop",
            englishTitle = "Cowboy Bebop",
            imageUrl = "https://media.kitsu.app/poster.jpg",
            largeImageUrl = "https://media.kitsu.app/poster-large.jpg",
            type = "TV",
            episodes = EPISODES,
            status = "finished",
            score = 8.42,
            ranking = RANK,
            members = 90_000,
            year = 1998,
            episodeMinutes = MINUTES,
            ageRating = "R",
            synopsis = "Sinopsis.",
            genres = listOf("Action"),
            trailerUrl = "https://www.youtube.com/watch?v=abc",
        )
    }
}

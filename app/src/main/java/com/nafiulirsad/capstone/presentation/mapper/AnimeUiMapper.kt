package com.nafiulirsad.capstone.presentation.mapper

import android.content.Context
import com.nafiulirsad.capstone.R
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.presentation.model.AnimeDetailUi
import com.nafiulirsad.capstone.presentation.model.AnimeUi
import java.util.Locale

/**
 * Turns the domain model into the display-ready presentation models. All formatting, fallbacks,
 * and translation of raw API vocabulary happen here, so the views stay free of logic.
 */
class AnimeUiMapper(private val context: Context) {

    fun toAnimeUi(anime: Anime): AnimeUi = AnimeUi(
        animeId = anime.animeId,
        title = anime.title,
        posterUrl = anime.imageUrl,
        scoreLabel = formatScore(anime.score),
        metaLabel = buildMetaLabel(anime),
    )

    fun toAnimeDetailUi(anime: Anime): AnimeDetailUi = AnimeDetailUi(
        animeId = anime.animeId,
        title = anime.title,
        subtitle = anime.englishTitle.takeIf { !it.isNullOrBlank() && it != anime.title }.orEmpty(),
        posterUrl = anime.imageUrl,
        backdropUrl = anime.largeImageUrl.ifBlank { anime.imageUrl },
        scoreLabel = formatScore(anime.score),
        rankLabel = anime.ranking?.let { context.getString(R.string.format_rank, it) } ?: PLACEHOLDER,
        membersLabel = anime.members?.let { formatCount(it) } ?: PLACEHOLDER,
        metaLabel = buildMetaLabel(anime),
        statusLabel = statusLabel(anime.status),
        durationLabel = anime.episodeMinutes
            ?.let { context.resources.getQuantityString(R.plurals.format_duration, it, it) }
            ?: context.getString(R.string.label_unknown),
        ageRatingLabel = anime.ageRating?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.label_unknown),
        synopsis = anime.synopsis?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.label_no_synopsis),
        genres = anime.genres,
        trailerUrl = anime.trailerUrl?.takeIf { it.isNotBlank() },
    )

    private fun formatScore(score: Double?): String =
        score?.let { String.format(Locale.US, "%.2f", it) } ?: PLACEHOLDER

    private fun buildMetaLabel(anime: Anime): String = listOfNotNull(
        anime.type?.takeIf { it.isNotBlank() }?.uppercase(Locale.US),
        anime.episodes?.let { context.resources.getQuantityString(R.plurals.format_episodes, it, it) },
        anime.year?.toString(),
    ).joinToString(SEPARATOR).ifBlank { context.getString(R.string.label_unknown) }

    /** Kitsu reports the airing state in its own vocabulary; the user should read Indonesian. */
    private fun statusLabel(status: String?): String = when (status?.lowercase(Locale.US)) {
        STATUS_FINISHED -> context.getString(R.string.status_finished)
        STATUS_CURRENT -> context.getString(R.string.status_airing)
        STATUS_UPCOMING, STATUS_UNRELEASED, STATUS_TBA -> context.getString(R.string.status_upcoming)
        else -> context.getString(R.string.label_unknown)
    }

    private fun formatCount(value: Int): String = when {
        value >= MILLION -> String.format(Locale.US, "%.1fJt", value / MILLION.toDouble())
        value >= THOUSAND -> String.format(Locale.US, "%.1frb", value / THOUSAND.toDouble())
        else -> value.toString()
    }

    private companion object {
        const val PLACEHOLDER = "N/A"
        const val SEPARATOR = " • "
        const val THOUSAND = 1_000
        const val MILLION = 1_000_000

        const val STATUS_FINISHED = "finished"
        const val STATUS_CURRENT = "current"
        const val STATUS_UPCOMING = "upcoming"
        const val STATUS_UNRELEASED = "unreleased"
        const val STATUS_TBA = "tba"
    }
}

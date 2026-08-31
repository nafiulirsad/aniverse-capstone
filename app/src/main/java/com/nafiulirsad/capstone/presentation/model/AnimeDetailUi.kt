package com.nafiulirsad.capstone.presentation.model

/** Presentation model for the detail screen. */
data class AnimeDetailUi(
    val animeId: Int,
    val title: String,
    val subtitle: String,
    val posterUrl: String,
    val backdropUrl: String,
    val scoreLabel: String,
    val rankLabel: String,
    val membersLabel: String,
    val metaLabel: String,
    val statusLabel: String,
    val durationLabel: String,
    val ageRatingLabel: String,
    val synopsis: String,
    val genres: List<String>,
    val trailerUrl: String?,
)

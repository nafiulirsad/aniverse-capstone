package com.nafiulirsad.capstone.core.domain.model

/**
 * Domain model: framework-free and non-nullable where the app needs a guarantee.
 * Neither Room nor Gson annotations are allowed here.
 */
data class Anime(
    val animeId: Int,
    val title: String,
    val englishTitle: String?,
    val imageUrl: String,
    val largeImageUrl: String,
    val type: String?,
    val episodes: Int?,
    val status: String?,
    val score: Double?,
    val ranking: Int?,
    val members: Int?,
    val year: Int?,
    val episodeMinutes: Int?,
    val ageRating: String?,
    val synopsis: String?,
    val genres: List<String>,
    val trailerUrl: String?,
)

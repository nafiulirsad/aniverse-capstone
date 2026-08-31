package com.nafiulirsad.capstone.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Data-layer model: the cached "discover" list. Wiped and refilled on every successful refresh. */
@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey
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

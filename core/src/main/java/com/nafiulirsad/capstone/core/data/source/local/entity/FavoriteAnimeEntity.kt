package com.nafiulirsad.capstone.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data-layer model: user-owned data. Kept in its own table so that clearing the network cache
 * can never delete a favorite, and so a favorite stays readable while offline.
 */
@Entity(tableName = "favorite_anime")
data class FavoriteAnimeEntity(
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
    val favoritedAt: Long,
)

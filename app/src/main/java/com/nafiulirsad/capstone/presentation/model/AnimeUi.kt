package com.nafiulirsad.capstone.presentation.model

/**
 * Presentation model for the list screens: everything is already a display-ready string,
 * so the adapter never has to format or null-check anything.
 */
data class AnimeUi(
    val animeId: Int,
    val title: String,
    val posterUrl: String,
    val scoreLabel: String,
    val metaLabel: String,
)

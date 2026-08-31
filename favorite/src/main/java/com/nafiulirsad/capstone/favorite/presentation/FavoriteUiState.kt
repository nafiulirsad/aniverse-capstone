package com.nafiulirsad.capstone.favorite.presentation

import com.nafiulirsad.capstone.presentation.model.AnimeUi

data class FavoriteUiState(val isLoading: Boolean = true, val items: List<AnimeUi> = emptyList()) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}

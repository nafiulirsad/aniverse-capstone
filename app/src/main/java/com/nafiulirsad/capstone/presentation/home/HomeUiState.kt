package com.nafiulirsad.capstone.presentation.home

import com.nafiulirsad.capstone.presentation.model.AnimeUi

data class HomeUiState(
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val items: List<AnimeUi> = emptyList(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty() && errorMessage == null
    val isBlockingError: Boolean get() = errorMessage != null && items.isEmpty()
}

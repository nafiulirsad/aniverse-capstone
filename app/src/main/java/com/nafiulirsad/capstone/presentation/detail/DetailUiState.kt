package com.nafiulirsad.capstone.presentation.detail

import com.nafiulirsad.capstone.presentation.model.AnimeDetailUi

data class DetailUiState(
    val isLoading: Boolean = true,
    val anime: AnimeDetailUi? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
) {
    val isBlockingError: Boolean get() = errorMessage != null && anime == null
}

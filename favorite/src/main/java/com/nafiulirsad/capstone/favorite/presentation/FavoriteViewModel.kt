package com.nafiulirsad.capstone.favorite.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nafiulirsad.capstone.core.domain.usecase.AnimeUseCase
import com.nafiulirsad.capstone.presentation.mapper.AnimeUiMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Reads straight from Room, so the list updates the moment a favorite is added or removed. */
class FavoriteViewModel(animeUseCase: AnimeUseCase, animeUiMapper: AnimeUiMapper) : ViewModel() {

    val uiState: StateFlow<FavoriteUiState> = animeUseCase.getFavoriteAnime()
        .map { favorites ->
            FavoriteUiState(isLoading = false, items = favorites.map(animeUiMapper::toAnimeUi))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FavoriteUiState())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

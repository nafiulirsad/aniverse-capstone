package com.nafiulirsad.capstone.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.usecase.AnimeUseCase
import com.nafiulirsad.capstone.presentation.mapper.AnimeUiMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    private val animeUseCase: AnimeUseCase,
    private val animeUiMapper: AnimeUiMapper,
    private val animeId: Int,
) : ViewModel() {

    /** Kept so the favorite toggle can persist the full domain object, not the UI model. */
    @Volatile
    private var loadedAnime: Anime? = null

    private val _favoriteEvent = MutableSharedFlow<Boolean>()
    val favoriteEvent: SharedFlow<Boolean> = _favoriteEvent.asSharedFlow()

    /** Detail data and favorite state come from two different sources, merged into one state. */
    val uiState: StateFlow<DetailUiState> = combine(
        animeUseCase.getAnimeDetail(animeId),
        animeUseCase.isFavorite(animeId),
    ) { resource, isFavorite ->
        resource.data?.let { loadedAnime = it }
        DetailUiState(
            isLoading = resource is Resource.Loading,
            anime = resource.data?.let(animeUiMapper::toAnimeDetailUi),
            isFavorite = isFavorite,
            errorMessage = resource.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DetailUiState())

    fun toggleFavorite() {
        val anime = loadedAnime ?: return
        val shouldFavorite = !uiState.value.isFavorite
        viewModelScope.launch {
            animeUseCase.setFavorite(anime, shouldFavorite)
            _favoriteEvent.emit(shouldFavorite)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

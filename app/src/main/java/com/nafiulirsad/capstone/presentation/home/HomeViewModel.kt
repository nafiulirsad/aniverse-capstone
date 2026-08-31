package com.nafiulirsad.capstone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.domain.model.Anime
import com.nafiulirsad.capstone.core.domain.usecase.AnimeUseCase
import com.nafiulirsad.capstone.presentation.mapper.AnimeUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * The whole screen is one reactive pipeline: query changes and pull-to-refresh both feed the same
 * stream, and `flatMapLatest` guarantees an outdated request can never win the race.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(private val animeUseCase: AnimeUseCase, private val animeUiMapper: AnimeUiMapper) : ViewModel() {

    private val request = MutableStateFlow(Request())

    val query: StateFlow<String> = request
        .map { it.query }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val uiState: StateFlow<HomeUiState> = request
        .debounce { if (it.query.isBlank()) NO_DEBOUNCE else SEARCH_DEBOUNCE_MS }
        .flatMapLatest { current ->
            val source = if (current.query.isBlank()) {
                animeUseCase.getTopAnime(forceRefresh = current.forceRefresh)
            } else {
                animeUseCase.searchAnime(current.query.trim())
            }
            source.map { resource -> resource.toUiState(isSearching = current.query.isNotBlank()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    fun onQueryChanged(value: String) {
        request.update { current ->
            if (current.query == value) {
                current
            } else {
                current.copy(query = value, forceRefresh = false, nonce = current.nonce + 1)
            }
        }
    }

    fun onRefresh() {
        request.update { current -> current.copy(forceRefresh = true, nonce = current.nonce + 1) }
    }

    private fun Resource<List<Anime>>.toUiState(isSearching: Boolean) = HomeUiState(
        isLoading = this is Resource.Loading,
        isSearching = isSearching,
        items = data?.map(animeUiMapper::toAnimeUi).orEmpty(),
        errorMessage = message,
    )

    /** `nonce` makes a repeated pull-to-refresh a distinct value, so the stream restarts. */
    private data class Request(
        val query: String = "",
        val forceRefresh: Boolean = false,
        val nonce: Int = 0,
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val NO_DEBOUNCE = 0L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

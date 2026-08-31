package com.nafiulirsad.capstone.presentation.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.nafiulirsad.capstone.R
import com.nafiulirsad.capstone.databinding.FragmentDetailBinding
import com.nafiulirsad.capstone.presentation.common.ARG_ANIME_ID
import com.nafiulirsad.capstone.presentation.common.loadPoster
import com.nafiulirsad.capstone.presentation.model.AnimeDetailUi
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val animeId: Int by lazy { requireArguments().getInt(ARG_ANIME_ID) }

    private val viewModel: DetailViewModel by viewModel { parametersOf(animeId) }

    /** Guards against re-showing the same offline notice on every state emission. */
    private var lastStaleMessage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.fabFavorite.setOnClickListener { viewModel.toggleFavorite() }
        setupToolbarMenu()
        observeUiState()
        observeFavoriteEvent()
    }

    /** Sharing is an extra beyond the three required features, so it lives in the toolbar menu. */
    private fun setupToolbarMenu() = with(binding.toolbar) {
        inflateMenu(R.menu.menu_detail)
        menu.findItem(R.id.action_share)?.isVisible = false
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share -> {
                    shareAnime()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun shareAnime() {
        val anime = viewModel.uiState.value.anime ?: return
        val shareText = getString(
            R.string.format_share,
            anime.title,
            anime.scoreLabel,
            KITSU_WEB_URL.format(Locale.US, anime.animeId),
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_TEXT
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
        } catch (_: ActivityNotFoundException) {
            showMessage(getString(R.string.error_no_share_target))
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun observeFavoriteEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteEvent.collect { isFavorite ->
                    showMessage(
                        getString(
                            if (isFavorite) R.string.favorite_added else R.string.favorite_removed,
                        ),
                    )
                }
            }
        }
    }

    private fun render(state: DetailUiState) = with(binding) {
        progressBar.isVisible = state.isLoading && state.anime == null
        groupError.isVisible = state.isBlockingError
        tvErrorMessage.text = state.errorMessage
        contentScroll.isVisible = state.anime != null
        fabFavorite.isVisible = state.anime != null
        toolbar.menu.findItem(R.id.action_share)?.isVisible = state.anime != null

        fabFavorite.setIconResource(
            if (state.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border,
        )
        fabFavorite.setText(
            if (state.isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite,
        )

        state.anime?.let(::bindAnime)

        val staleMessage = state.errorMessage?.takeIf { state.anime != null }
        if (staleMessage != null && staleMessage != lastStaleMessage) {
            lastStaleMessage = staleMessage
            showMessage(getString(R.string.detail_showing_cache))
        }
    }

    private fun bindAnime(anime: AnimeDetailUi) = with(binding) {
        collapsingToolbar.title = anime.title
        imgBackdrop.loadPoster(anime.backdropUrl)
        imgPoster.loadPoster(anime.posterUrl)
        imgPoster.contentDescription = anime.title

        tvTitle.text = anime.title
        tvSubtitle.isVisible = anime.subtitle.isNotBlank()
        tvSubtitle.text = anime.subtitle
        tvScore.text = anime.scoreLabel
        tvRank.text = anime.rankLabel
        tvMembers.text = anime.membersLabel
        tvMeta.text = anime.metaLabel
        tvStatus.text = anime.statusLabel
        tvDuration.text = anime.durationLabel
        tvAgeRating.text = anime.ageRatingLabel
        tvSynopsis.text = anime.synopsis

        bindGenres(anime)

        btnTrailer.isVisible = anime.trailerUrl != null
        btnTrailer.setOnClickListener { anime.trailerUrl?.let(::openTrailer) }
    }

    private fun bindGenres(anime: AnimeDetailUi) = with(binding.chipGroupGenre) {
        removeAllViews()
        isVisible = anime.genres.isNotEmpty()
        anime.genres.forEach { genre ->
            addView(
                Chip(requireContext()).apply {
                    text = genre
                    isClickable = false
                    isCheckable = false
                },
            )
        }
    }

    private fun openTrailer(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (_: ActivityNotFoundException) {
            showMessage(getString(R.string.error_no_browser))
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val KITSU_WEB_URL = "https://kitsu.io/anime/%d"
        const val MIME_TYPE_TEXT = "text/plain"
    }
}

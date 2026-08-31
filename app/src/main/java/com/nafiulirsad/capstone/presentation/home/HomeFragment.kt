package com.nafiulirsad.capstone.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nafiulirsad.capstone.R
import com.nafiulirsad.capstone.databinding.FragmentHomeBinding
import com.nafiulirsad.capstone.presentation.common.AnimeAdapter
import com.nafiulirsad.capstone.presentation.common.navigateToAnimeDetail
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: HomeViewModel by viewModel()

    /** Guards against re-showing the same offline notice on every state emission. */
    private var lastStaleMessage: String? = null

    private val animeAdapter by lazy {
        AnimeAdapter { anime -> findNavController().navigateToAnimeDetail(anime.animeId) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchInput()
        setupRefresh()
        observeUiState()
    }

    private fun setupRecyclerView() = with(binding.rvAnime) {
        layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        adapter = animeAdapter
        setHasFixedSize(true)
    }

    private fun setupSearchInput() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.onRefresh() }
        binding.btnRetry.setOnClickListener { viewModel.onRefresh() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HomeUiState) = with(binding) {
        animeAdapter.submitList(state.items)

        swipeRefresh.isRefreshing = state.isLoading && state.items.isNotEmpty()
        progressBar.isVisible = state.isLoading && state.items.isEmpty()
        rvAnime.isVisible = state.items.isNotEmpty()

        groupError.isVisible = state.isBlockingError
        tvErrorMessage.text = state.errorMessage

        groupEmpty.isVisible = state.isEmpty
        tvEmptyMessage.setText(
            if (state.isSearching) R.string.empty_search else R.string.empty_home,
        )

        showStaleNoticeIfNeeded(state)
    }

    /** Results are on screen but they came from the cache, so say so once. */
    private fun showStaleNoticeIfNeeded(state: HomeUiState) {
        val staleMessage = state.errorMessage?.takeIf { state.items.isNotEmpty() }
        if (staleMessage == null || staleMessage == lastStaleMessage) return

        lastStaleMessage = staleMessage
        Snackbar.make(binding.root, R.string.home_showing_cache, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        binding.rvAnime.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val SPAN_COUNT = 2
    }
}

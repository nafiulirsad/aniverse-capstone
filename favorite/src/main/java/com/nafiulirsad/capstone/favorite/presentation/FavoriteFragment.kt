package com.nafiulirsad.capstone.favorite.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.nafiulirsad.capstone.favorite.databinding.FragmentFavoriteBinding
import com.nafiulirsad.capstone.favorite.di.favoriteModule
import com.nafiulirsad.capstone.presentation.common.AnimeAdapter
import com.nafiulirsad.capstone.presentation.common.navigateToAnimeDetail
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: FavoriteViewModel by viewModel()

    private val animeAdapter by lazy {
        AnimeAdapter { anime -> findNavController().navigateToAnimeDetail(anime.animeId) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(favoriteModule)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUiState()
    }

    private fun setupRecyclerView() = with(binding.rvFavorite) {
        layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        adapter = animeAdapter
        setHasFixedSize(true)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: FavoriteUiState) = with(binding) {
        animeAdapter.submitList(state.items)
        progressBar.isVisible = state.isLoading
        rvFavorite.isVisible = state.items.isNotEmpty()
        groupEmpty.isVisible = state.isEmpty
    }

    override fun onDestroyView() {
        binding.rvFavorite.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        unloadKoinModules(favoriteModule)
        super.onDestroy()
    }

    private companion object {
        const val SPAN_COUNT = 2
    }
}

package com.nafiulirsad.capstone.favorite.di

import com.nafiulirsad.capstone.favorite.presentation.FavoriteViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The dynamic feature owns its own Koin module. It cannot be registered at app start, because the
 * classes only exist once the module is installed, so [com.nafiulirsad.capstone.favorite.presentation.FavoriteFragment]
 * loads it on demand and unloads it again when the screen goes away.
 */
val favoriteModule = module {
    viewModel { FavoriteViewModel(get(), get()) }
}

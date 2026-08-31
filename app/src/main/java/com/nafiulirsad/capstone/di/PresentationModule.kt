package com.nafiulirsad.capstone.di

import com.nafiulirsad.capstone.presentation.detail.DetailViewModel
import com.nafiulirsad.capstone.presentation.home.HomeViewModel
import com.nafiulirsad.capstone.presentation.main.MainViewModel
import com.nafiulirsad.capstone.presentation.mapper.AnimeUiMapper
import com.nafiulirsad.capstone.presentation.setting.SettingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Presentation scopes:
 *  - `single` for the stateless mapper that every screen (including the dynamic feature) reuses,
 *  - `viewModel` so each ViewModel lives exactly as long as its owner.
 */
val presentationModule = module {
    single { AnimeUiMapper(androidContext()) }

    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SettingViewModel(get()) }
    viewModel { (animeId: Int) -> DetailViewModel(get(), get(), animeId) }
}

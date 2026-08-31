package com.nafiulirsad.capstone.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.core.domain.usecase.SettingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(settingUseCase: SettingUseCase) : ViewModel() {

    /** `null` until the stored value has actually been read, so nothing acts on a placeholder. */
    val themeMode: StateFlow<ThemeMode?> = settingUseCase.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

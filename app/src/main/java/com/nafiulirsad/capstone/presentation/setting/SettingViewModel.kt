package com.nafiulirsad.capstone.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.core.domain.usecase.SettingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(private val settingUseCase: SettingUseCase) : ViewModel() {

    /**
     * `null` while the stored value is still being read. Emitting a placeholder here would make
     * the radio group check a value the user never picked, and that selection would be written
     * straight back to DataStore.
     */
    val themeMode: StateFlow<ThemeMode?> = settingUseCase.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun onThemeModeSelected(themeMode: ThemeMode) {
        if (themeMode == this.themeMode.value) return

        viewModelScope.launch { settingUseCase.setThemeMode(themeMode) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

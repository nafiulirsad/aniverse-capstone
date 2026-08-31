package com.nafiulirsad.capstone.core.domain.usecase

import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingUseCase {
    fun getThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(themeMode: ThemeMode)
}

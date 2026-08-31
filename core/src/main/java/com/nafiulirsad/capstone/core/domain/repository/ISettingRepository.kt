package com.nafiulirsad.capstone.core.domain.repository

import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ISettingRepository {
    fun getThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(themeMode: ThemeMode)
}

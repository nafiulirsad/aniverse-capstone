package com.nafiulirsad.capstone.core.data

import com.nafiulirsad.capstone.core.data.source.local.preference.SettingPreference
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.core.domain.repository.ISettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingRepository(private val settingPreference: SettingPreference) : ISettingRepository {

    override fun getThemeMode(): Flow<ThemeMode> =
        settingPreference.themeMode.map { ThemeMode.fromName(it) }

    override suspend fun setThemeMode(themeMode: ThemeMode) =
        settingPreference.setThemeMode(themeMode.name)
}

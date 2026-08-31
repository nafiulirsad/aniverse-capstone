package com.nafiulirsad.capstone.core.domain.usecase

import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.core.domain.repository.ISettingRepository
import kotlinx.coroutines.flow.Flow

class SettingInteractor(private val settingRepository: ISettingRepository) : SettingUseCase {

    override fun getThemeMode(): Flow<ThemeMode> = settingRepository.getThemeMode()

    override suspend fun setThemeMode(themeMode: ThemeMode) =
        settingRepository.setThemeMode(themeMode)
}

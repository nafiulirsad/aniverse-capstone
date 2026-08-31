package com.nafiulirsad.capstone.presentation.setting

import app.cash.turbine.test
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.core.domain.usecase.SettingUseCase
import com.nafiulirsad.capstone.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingUseCase: SettingUseCase = mockk()

    @Test
    fun `the stored theme is exposed once it has actually been read`() = runTest {
        every { settingUseCase.getThemeMode() } returns flowOf(ThemeMode.DARK)

        val viewModel = SettingViewModel(settingUseCase)

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun `picking a new theme writes it back`() = runTest {
        every { settingUseCase.getThemeMode() } returns flowOf(ThemeMode.SYSTEM)
        coEvery { settingUseCase.setThemeMode(ThemeMode.LIGHT) } just Runs

        val viewModel = SettingViewModel(settingUseCase)
        viewModel.themeMode.test { awaitItem() }
        viewModel.onThemeModeSelected(ThemeMode.LIGHT)

        coVerify(exactly = 1) { settingUseCase.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `re-picking the theme that is already active writes nothing`() = runTest {
        every { settingUseCase.getThemeMode() } returns flowOf(ThemeMode.DARK)

        val viewModel = SettingViewModel(settingUseCase)
        viewModel.themeMode.test { awaitItem() }
        viewModel.onThemeModeSelected(ThemeMode.DARK)

        coVerify(exactly = 0) { settingUseCase.setThemeMode(any()) }
    }
}

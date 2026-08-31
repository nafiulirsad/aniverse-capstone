package com.nafiulirsad.capstone

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.nafiulirsad.capstone.core.di.databaseModule
import com.nafiulirsad.capstone.core.di.networkModule
import com.nafiulirsad.capstone.core.di.repositoryModule
import com.nafiulirsad.capstone.core.di.useCaseModule
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.core.domain.usecase.SettingUseCase
import com.nafiulirsad.capstone.di.presentationModule
import com.nafiulirsad.capstone.presentation.common.toNightMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Composition root. Every dependency in the app is created here, no manual injection anywhere else.
 * The dynamic feature adds its own module at runtime, once it is actually opened.
 */
class AniVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@AniVerseApplication)
            modules(
                databaseModule,
                networkModule,
                repositoryModule,
                useCaseModule,
                presentationModule,
            )
        }
        applyStoredTheme()
    }

    /**
     * The saved theme has to be in place before the first Activity inflates, otherwise the app
     * paints one frame in the system theme and then jumps to the chosen one. Reading a single
     * DataStore key costs a couple of milliseconds, and the timeout keeps a stuck read from
     * blocking startup.
     */
    private fun applyStoredTheme() {
        val settingUseCase = get<SettingUseCase>()
        val storedMode = runBlocking {
            withTimeoutOrNull(THEME_READ_TIMEOUT_MS) { settingUseCase.getThemeMode().first() }
        } ?: ThemeMode.SYSTEM

        AppCompatDelegate.setDefaultNightMode(storedMode.toNightMode())
    }

    private companion object {
        const val THEME_READ_TIMEOUT_MS = 500L
    }
}

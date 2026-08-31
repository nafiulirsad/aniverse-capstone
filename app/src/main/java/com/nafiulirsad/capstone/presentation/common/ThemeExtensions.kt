package com.nafiulirsad.capstone.presentation.common

import androidx.appcompat.app.AppCompatDelegate
import com.nafiulirsad.capstone.core.domain.model.ThemeMode

/** The one place that knows how a domain [ThemeMode] maps onto an AppCompat night mode. */
fun ThemeMode.toNightMode(): Int = when (this) {
    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}

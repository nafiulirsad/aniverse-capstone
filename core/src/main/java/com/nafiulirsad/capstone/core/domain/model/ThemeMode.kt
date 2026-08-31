package com.nafiulirsad.capstone.core.domain.model

/** Domain model for the appearance setting, free of any AppCompat constant. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

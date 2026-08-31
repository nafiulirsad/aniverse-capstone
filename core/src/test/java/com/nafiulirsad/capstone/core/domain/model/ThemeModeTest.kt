package com.nafiulirsad.capstone.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `a stored name maps back to the matching mode`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
    }

    @Test
    fun `an unknown or missing name falls back to the system theme`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("SEPIA"))
    }
}

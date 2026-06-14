package com.example.miyad.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun parsesPersistedThemeValues() {
        assertEquals(ThemeMode.System, ThemeMode.fromStorage("system"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage("light"))
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage("dark"))
    }

    @Test
    fun invalidPersistedThemeFallsBackToSystem() {
        assertEquals(ThemeMode.System, ThemeMode.fromStorage("sepia"))
        assertEquals(ThemeMode.System, ThemeMode.fromStorage(null))
    }
}

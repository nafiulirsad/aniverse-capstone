package com.nafiulirsad.capstone.core.data.source.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore(name = "aniverse_setting")

class SettingPreference(context: Context) {

    private val dataStore = context.settingDataStore

    val themeMode: Flow<String?> = dataStore.data.map { it[THEME_MODE_KEY] }

    suspend fun setThemeMode(value: String) {
        dataStore.edit { preferences -> preferences[THEME_MODE_KEY] = value }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}

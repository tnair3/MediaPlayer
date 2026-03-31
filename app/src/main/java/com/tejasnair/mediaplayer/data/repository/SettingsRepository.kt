package com.tejasnair.mediaplayer.data.repository

// 1. Android & Core
import android.content.Context

// 2. DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// 3. Coroutines & Flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 4. Local Project Imports
import com.tejasnair.mediaplayer.ui.components.ThemeMode

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    // This Flow reads from disk and emits a new value whenever the file changes
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    // This function writes the user's choice to disk
    suspend fun saveThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }
}
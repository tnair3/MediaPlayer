package com.tejasnair.mediaplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.data.repository.SettingsRepository
import com.tejasnair.mediaplayer.ui.components.ThemeMode
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val themeSetting: StateFlow<ThemeMode> = repository.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun updateTheme(newSetting: ThemeMode) {
        // We launch a coroutine to save the value to disk asynchronously
        viewModelScope.launch {
            repository.saveThemeMode(newSetting)
        }
    }
}
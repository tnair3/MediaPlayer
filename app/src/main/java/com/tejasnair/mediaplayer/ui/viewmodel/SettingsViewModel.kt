package com.tejasnair.mediaplayer.ui.viewmodel

// 1. Android & Core
import android.app.Application

// 2. Lifecycle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

// 3. Coroutines & Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 4. Local Project Imports
import com.tejasnair.mediaplayer.data.repository.SettingsRepository
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    
}
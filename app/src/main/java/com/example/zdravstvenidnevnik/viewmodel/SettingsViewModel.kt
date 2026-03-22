package com.example.zdravstvenidnevnik.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zdravstvenidnevnik.data.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppSettingsRepository(application)

    val darkModeEnabled: StateFlow<Boolean?> = repository.darkModeEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val languageTag: StateFlow<String> = repository.languageTag
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkModeEnabled(enabled)
        }
    }

    fun setLanguageTag(languageTag: String) {
        viewModelScope.launch {
            repository.setLanguageTag(languageTag)
        }
    }
}

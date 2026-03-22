package com.example.zdravstvenidnevnik.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(
    private val context: Context
) {
    private object Keys {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    }

    val darkModeEnabled: Flow<Boolean?> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.DARK_MODE_ENABLED]
        }

    val languageTag: Flow<String> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.LANGUAGE_TAG] ?: ""
        }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setLanguageTag(languageTag: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LANGUAGE_TAG] = languageTag
        }
    }
}

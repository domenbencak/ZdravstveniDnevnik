package com.example.zdravstvenidnevnik

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import com.example.zdravstvenidnevnik.ui.navigation.MeritevNavHost
import com.example.zdravstvenidnevnik.ui.theme.ZdravstveniDnevnikTheme
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel
import com.example.zdravstvenidnevnik.viewmodel.SettingsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val meritevViewModel: MeritevViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsStateWithLifecycle()
            val languageTag by settingsViewModel.languageTag.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()

            LaunchedEffect(languageTag) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageTag)
                )
            }

            ZdravstveniDnevnikTheme(
                darkTheme = darkModeEnabled ?: systemDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeritevNavHost(
                        navController = navController,
                        viewModel = meritevViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

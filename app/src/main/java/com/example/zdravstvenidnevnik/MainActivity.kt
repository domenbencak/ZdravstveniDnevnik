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
import androidx.activity.SystemBarStyle
import com.example.zdravstvenidnevnik.ui.navigation.MeritevNavHost
import com.example.zdravstvenidnevnik.ui.theme.ZdravstveniDnevnikTheme
import com.example.zdravstvenidnevnik.viewmodel.AuthViewModel
import com.example.zdravstvenidnevnik.viewmodel.HealthViewModel
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
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            val healthViewModel: HealthViewModel = viewModel()
            val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsStateWithLifecycle()
            val languageTag by settingsViewModel.languageTag.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = darkModeEnabled ?: systemDarkTheme

            LaunchedEffect(languageTag) {
                if (languageTag == null) return@LaunchedEffect
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageTag ?: "")
                )
            }

            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }

            ZdravstveniDnevnikTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeritevNavHost(
                        navController = navController,
                        viewModel = meritevViewModel,
                        healthViewModel = healthViewModel,
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

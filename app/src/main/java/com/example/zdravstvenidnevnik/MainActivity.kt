package com.example.zdravstvenidnevnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.zdravstvenidnevnik.ui.navigation.MeritevNavHost
import com.example.zdravstvenidnevnik.ui.theme.ZdravstveniDnevnikTheme
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZdravstveniDnevnikTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: MeritevViewModel = viewModel()
                    MeritevNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
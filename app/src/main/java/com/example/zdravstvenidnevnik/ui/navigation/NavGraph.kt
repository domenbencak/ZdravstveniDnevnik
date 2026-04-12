package com.example.zdravstvenidnevnik.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.zdravstvenidnevnik.ui.screens.*
import com.example.zdravstvenidnevnik.viewmodel.AuthViewModel
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel
import com.example.zdravstvenidnevnik.viewmodel.SettingsViewModel

@Composable
fun MeritevNavHost(
    navController: NavHostController,
    viewModel: MeritevViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel
) {
    val authUiState = authViewModel.uiState.collectAsStateWithLifecycle().value
    val startDestination = if (authUiState.currentUser == null) "auth" else "vnos"
    val navigateToAuth: () -> Unit = {
        navController.navigate("auth") {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
    val logoutAndNavigateToAuth: () -> Unit = {
        authViewModel.logout()
        navigateToAuth()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate("vnos") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("vnos") {
            VnosScreen(
                viewModel = viewModel,
                editMeritevId = null,
                onMeritevSaved = { id ->
                    navController.navigate("prikaz/$id")
                },
                onNavigateToSeznam = {
                    navController.navigate("seznam")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                currentUserDisplayName = authUiState.currentUser?.displayName.orEmpty()
            )
        }

        composable(
            route = "vnos/{meritevId}",
            arguments = listOf(
                navArgument("meritevId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val meritevId = backStackEntry.arguments?.getInt("meritevId") ?: return@composable
            VnosScreen(
                viewModel = viewModel,
                editMeritevId = meritevId,
                onMeritevSaved = { id ->
                    navController.navigate("prikaz/$id")
                },
                onNavigateToSeznam = {
                    navController.navigate("seznam")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                currentUserDisplayName = authUiState.currentUser?.displayName.orEmpty()
            )
        }

        composable(
            route = "prikaz/{meritevId}",
            arguments = listOf(
                navArgument("meritevId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val meritevId = backStackEntry.arguments
                ?.getInt("meritevId") ?: return@composable
            PrikazScreen(
                viewModel = viewModel,
                meritevId = meritevId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("seznam") {
            SeznamScreen(
                viewModel = viewModel,
                onOpenDetails = { id -> navController.navigate("prikaz/$id") },
                onEditMeasurement = { id -> navController.navigate("vnos/$id") },
                onNavigateBack = { navController.popBackStack() },
                onAddMeasurement = { navController.navigate("vnos") },
                onNavigateToSettings = { navController.navigate("settings") },
                onSyncFromCloud = { viewModel.syncFromFirestore() },
                onLogout = logoutAndNavigateToAuth,
                loggedInEmail = authUiState.currentUser?.email.orEmpty()
            )
        }

        composable("settings") {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate("settings/profile") }
            )
        }

        composable("settings/profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = navigateToAuth
            )
        }
    }
}

package com.example.zdravstvenidnevnik.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.zdravstvenidnevnik.ui.screens.*
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel

@Composable
fun MeritevNavHost(
    navController: NavHostController,
    viewModel: MeritevViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "vnos"
    ) {
        composable("vnos") {
            VnosScreen(
                viewModel = viewModel,
                editMeritevId = null,
                onMeritevSaved = { id ->
                    navController.navigate("prikaz/$id")
                },
                onNavigateToSeznam = {
                    navController.navigate("seznam")
                }
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
                }
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
                }
            )
        }

        composable("seznam") {
            SeznamScreen(
                viewModel = viewModel,
                onOpenDetails = { id -> navController.navigate("prikaz/$id") },
                onEditMeasurement = { id -> navController.navigate("vnos/$id") },
                onNavigateBack = { navController.popBackStack() },
                onAddMeasurement = { navController.navigate("vnos") }
            )
        }
    }
}

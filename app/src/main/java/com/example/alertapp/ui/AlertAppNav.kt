package com.example.alertapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.alertapp.ui.screens.AlertsListScreen
import com.example.alertapp.ui.screens.AlertVideoScreen
import com.example.alertapp.ui.screens.SettingsScreen

object Routes {
    const val ALERTS = "alerts"
    const val ALERT_VIDEO = "alerts/{alertId}"
    const val SETTINGS = "settings"

    fun alertVideo(alertId: String) = "alerts/$alertId"
}

@Composable
fun AlertAppNav(
    startAlertId: String? = null,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (!startAlertId.isNullOrBlank()) Routes.alertVideo(startAlertId) else Routes.ALERTS
    ) {
        composable(Routes.ALERTS) {
            AlertsListScreen(
                onAlertClick = { id -> navController.navigate(Routes.alertVideo(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.ALERT_VIDEO,
            arguments = listOf(navArgument("alertId") { defaultValue = startAlertId ?: "" })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
            AlertVideoScreen(alertId = alertId, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

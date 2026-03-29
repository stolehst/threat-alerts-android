package com.example.alertapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.example.alertapp.auth.AuthTokenStore
import com.example.alertapp.ui.screens.AlertDetailScreen
import com.example.alertapp.ui.screens.AlertsListScreen
import com.example.alertapp.ui.screens.AlertVideoScreen
import com.example.alertapp.ui.screens.ActivationScreen
import com.example.alertapp.ui.screens.SettingsScreen

object Routes {
    const val ACTIVATE = "activate"
    const val ALERTS = "alerts"
    const val ALERT_DETAIL = "alert/{alertId}/detail"
    const val ALERT_VIDEO = "alert/{alertId}/video"
    const val SETTINGS = "settings"

    fun alertDetail(id: String) = "alert/$id/detail"
    fun alertVideo(id: String) = "alert/$id/video"
}

@Composable
fun AlertAppNav(
    startAlertId: String? = null,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val hasToken = !AuthTokenStore.getApiToken(context).isNullOrBlank()
    NavHost(
        navController = navController,
        startDestination = if (!hasToken) Routes.ACTIVATE
        else if (!startAlertId.isNullOrBlank()) Routes.alertDetail(startAlertId) else Routes.ALERTS
    ) {
        composable(Routes.ACTIVATE) {
            ActivationScreen(
                onActivated = {
                    navController.navigate(Routes.ALERTS) {
                        popUpTo(Routes.ACTIVATE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.ALERTS) {
            AlertsListScreen(
                onAlertClick = { id -> navController.navigate(Routes.alertDetail(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.ALERT_DETAIL,
            arguments = listOf(navArgument("alertId") { defaultValue = startAlertId ?: "" })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
            AlertDetailScreen(
                alertId = alertId,
                onBack = { navController.popBackStack() },
                onOpenVideo = { navController.navigate(Routes.alertVideo(alertId)) }
            )
        }
        composable(
            route = Routes.ALERT_VIDEO,
            arguments = listOf(navArgument("alertId") { defaultValue = "" })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
            AlertVideoScreen(alertId = alertId, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.ACTIVATE) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

package com.example.alertapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ThreatAlertsDarkScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = Color.White,
    secondary = PrimaryBlue,
    onSecondary = Color.White,
    error = AlertRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = CardSurface,
    onSurface = OnDarkBackground,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnDarkMuted,
    outline = OnDarkMuted.copy(alpha = 0.5f)
)

private val ThreatAlertsLightScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    error = AlertRed,
    onError = Color.White,
    background = TopBarLight,
    onBackground = OnTopBarLight,
    surface = Color.White,
    onSurface = OnTopBarLight
)

@Composable
fun AlertAppTheme(
    darkTheme: Boolean = true,
    /** On Android 12+, tints primary/surface from wallpaper (Material You); alert red stays fixed. */
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val base = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            base.copy(
                error = AlertRed,
                onError = Color.White
            )
        }
        darkTheme -> ThreatAlertsDarkScheme
        else -> ThreatAlertsLightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode && view.context is Activity) {
        SideEffect {
            val window = (view.context as Activity).window
            // Match hardcoded Scaffold `DarkBackground`; dynamic color only affects MaterialTheme tokens in composables.
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

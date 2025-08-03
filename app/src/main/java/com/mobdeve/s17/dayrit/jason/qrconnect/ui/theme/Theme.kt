package com.mobdeve.s17.dayrit.jason.qrconnect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

// dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = QRBlue80,
    onPrimary = Color.Black,
    primaryContainer = QRBlue40,
    onPrimaryContainer = QRBlue80,

    secondary = QRBlueGrey80,
    onSecondary = Color.Black,
    secondaryContainer = QRBlueGrey40,
    onSecondaryContainer = QRBlueGrey80,

    tertiary = QRTeal80,
    onTertiary = Color.Black,
    tertiaryContainer = QRTeal40,
    onTertiaryContainer = QRTeal80,

    background = QRBackgroundDark,
    onBackground = Color.White,
    surface = QRSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = QRSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFE0E0E0),

    error = QRError,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242),
    scrim = Color.Black,

    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = QRBlue40
)

// light color scheme
private val LightColorScheme = lightColorScheme(
    primary = QRBlue40,
    onPrimary = Color.White,
    primaryContainer = QRBlue80,
    onPrimaryContainer = Color.Black,

    secondary = QRBlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = QRBlueGrey80,
    onSecondaryContainer = Color.Black,

    tertiary = QRTeal40,
    onTertiary = Color.White,
    tertiaryContainer = QRTeal80,
    onTertiaryContainer = Color.Black,

    background = QRBackground,
    onBackground = Color.Black,
    surface = QRSurface,
    onSurface = Color.Black,
    surfaceVariant = QRSurfaceVariant,
    onSurfaceVariant = Color(0xFF424242),

    error = QRError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFE0E0E0),
    scrim = Color.Black,

    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = QRBlue80
)

@Composable
fun MOBICOMQRConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // dynamic color is available on android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
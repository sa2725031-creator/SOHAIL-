package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SagePrimaryDark,
    onPrimary = OnSagePrimaryDark,
    primaryContainer = SagePrimaryContainerDark,
    onPrimaryContainer = OnSagePrimaryContainerDark,
    secondary = EucalyptusSecondaryDark,
    onSecondary = OnEucalyptusSecondaryDark,
    secondaryContainer = EucalyptusContainerDark,
    onSecondaryContainer = OnEucalyptusContainerDark,
    tertiary = CedarTertiaryDark,
    onTertiary = OnCedarTertiaryDark,
    tertiaryContainer = CedarContainerDark,
    onTertiaryContainer = OnCedarContainerDark,
    background = ZenBackgroundDark,
    surface = ZenSurfaceDark,
    surfaceVariant = ZenSurfaceVariantDark,
    onBackground = OnZenTextDark,
    onSurface = OnZenTextDark,
    onSurfaceVariant = OnZenTextVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = SagePrimaryLight,
    onPrimary = OnSagePrimaryLight,
    primaryContainer = SagePrimaryContainerLight,
    onPrimaryContainer = OnSagePrimaryContainerLight,
    secondary = EucalyptusSecondaryLight,
    onSecondary = OnEucalyptusSecondaryLight,
    secondaryContainer = EucalyptusContainerLight,
    onSecondaryContainer = OnEucalyptusContainerLight,
    tertiary = CedarTertiaryLight,
    onTertiary = OnCedarTertiaryLight,
    tertiaryContainer = CedarContainerLight,
    onTertiaryContainer = OnCedarContainerLight,
    background = ZenBackgroundLight,
    surface = ZenSurfaceLight,
    surfaceVariant = ZenSurfaceVariantLight,
    onBackground = OnZenTextLight,
    onSurface = OnZenTextLight,
    onSurfaceVariant = OnZenTextVariantLight
)

@Composable
fun SereneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep calm custom palette consistent
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

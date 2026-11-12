package aefox.reyang.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun MaterialTemplateTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val amoledMode = appSettings.colorMode.isAmoled
    val dynamicColor = appSettings.keyColor == 0
    val colorStyle = appSettings.paletteStyle
    val colorSpec = appSettings.colorSpec

    val colorScheme = if (dynamicColor) {
        val baseScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        rememberDynamicColorScheme(
            seedColor = Color.Unspecified,
            isDark = darkTheme,
            isAmoled = amoledMode,
            style = colorStyle,
            specVersion = colorSpec,
            primary = baseScheme.primary,
            secondary = baseScheme.secondary,
            tertiary = baseScheme.tertiary,
            neutral = baseScheme.surface,
            neutralVariant = baseScheme.surfaceVariant,
            error = baseScheme.error
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = Color(appSettings.keyColor),
            isDark = darkTheme,
            isAmoled = amoledMode,
            style = colorStyle,
            specVersion = colorSpec,
        )
    }

    // Theme package static color overlay: replaces only provided tokens; keeps the generated scheme when no overlay or parse fails.
    val overlayTokens = LocalThemeVisuals.current.appearance?.colorsFor(darkTheme)
    val themedScheme = remember(colorScheme, overlayTokens) {
        colorScheme.withAppearance(overlayTokens)
    }
    // Frosted-glass pass (mirrors SukiSU MaterialKernelSUTheme): while a theme
    // package background is active the surface family goes translucent so
    // cards, search bars, dialogs and bottom sheets blur the wallpaper. Applied
    // after the overlay so package colors participate; unchanged without a
    // background.
    val hasCustomBackground = LocalHasCustomBackground.current
    val cardAlpha = LocalCardBackgroundAlpha.current
    val surfaceAwareScheme = remember(themedScheme, hasCustomBackground, cardAlpha) {
        if (hasCustomBackground) {
            themedScheme.withBackgroundGlass(cardAlpha)
        } else {
            themedScheme
        }
    }

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = surfaceAwareScheme,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

/**
 * Translucent card surfaces over a theme package background.
 *
 * Mirrors SukiSU: only the card roles ([ColorScheme.surfaceBright],
 * [ColorScheme.surfaceContainerLowest], [ColorScheme.surfaceContainerHighest])
 * go translucent. Menu (`surfaceContainer`), dialog (`surfaceContainerHigh`)
 * and page (`surface`) roles stay opaque so overlays keep their layering and
 * remain readable over a bright wallpaper. Cards that use other roles get the
 * opacity from `backgroundAwareColor()` at the component level.
 */
private fun ColorScheme.withBackgroundGlass(alpha: Float): ColorScheme = copy(
    surfaceBright = surfaceBright.copy(alpha = alpha),
    surfaceContainerLowest = surfaceContainerLowest.copy(alpha = alpha),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = alpha),
)

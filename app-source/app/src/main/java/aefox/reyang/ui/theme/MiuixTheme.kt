package aefox.reyang.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Composable
fun MiuixTemplateTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val colorStyle = appSettings.paletteStyle
    val colorSpec = appSettings.colorSpec
    // Theme package static color overlay: picks the section for the current light/dark theme (shared applied first, then light/dark overlays).
    val overlayTokens = LocalThemeVisuals.current.appearance?.colorsFor(darkTheme)

    val miuixPaletteStyle = try {
        ThemePaletteStyle.valueOf(colorStyle.name)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }

    val miuixColorSpec = if (colorSpec == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }

    val controller = ThemeController(
        when (appSettings.colorMode) {
            ColorMode.SYSTEM -> ColorSchemeMode.System
            ColorMode.LIGHT -> ColorSchemeMode.Light
            ColorMode.DARK -> ColorSchemeMode.Dark
            ColorMode.MONET_SYSTEM -> ColorSchemeMode.MonetSystem
            ColorMode.MONET_LIGHT -> ColorSchemeMode.MonetLight
            ColorMode.MONET_DARK, ColorMode.DARK_AMOLED -> ColorSchemeMode.MonetDark
        },
        keyColor = if (appSettings.keyColor == 0) null else Color(appSettings.keyColor),
        isDark = darkTheme,
        paletteStyle = miuixPaletteStyle,
        colorSpec = miuixColorSpec,
    )

    MiuixTheme(
        controller = controller,
        content = {
            // Replace only tokens provided by the overlay; keep the generated scheme when no overlay or parse fails.
            val themedColors = MiuixTheme.colorScheme.withAppearance(overlayTokens)
            // Frosted-glass pass (mirrors SukiSU): while a theme package background is
            // active the whole surface family goes translucent so cards, dialogs and
            // menus blur the wallpaper. Applied after the overlay so package colors
            // participate too; disabled text gets SukiSU's contrast raise to stay legible.
            val hasCustomBackground = LocalHasCustomBackground.current
            val cardAlpha = LocalCardBackgroundAlpha.current
            val glassColors = if (hasCustomBackground) {
                themedColors.withBackgroundGlass(cardAlpha)
            } else {
                themedColors
            }
            MiuixTheme(glassColors) {
                LaunchedEffect(darkTheme) {
                    val window = (context as? Activity)?.window ?: return@LaunchedEffect
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                CompositionLocalProvider(
                    LocalContentColor provides MiuixTheme.colorScheme.onBackground,
                ) {
                    content()
                }
            }
        }
    )
}

/**
 * Translucent card containers over a theme package background.
 *
 * Only [Colors.surfaceContainer] (Miuix `Card`'s default color) and
 * [Colors.surfaceVariant] go translucent. Dialog and popup tokens
 * ([Colors.background], [Colors.surface], [Colors.surfaceContainerHigh])
 * must stay opaque: a translucent dialog lets the page underneath show
 * through and destroys the modal layering, and a translucent popup makes
 * the menu unreadable over a bright wallpaper.
 * Disabled text keeps SukiSU's raised contrast (alpha 0.85) because the
 * default ~38% mute is illegible over a translucent card.
 */
private fun Colors.withBackgroundGlass(alpha: Float): Colors = copy(
    surfaceContainer = surfaceContainer.copy(alpha = alpha),
    surfaceVariant = surfaceVariant.copy(alpha = alpha),
    disabledOnSurface = onSurface.copy(alpha = 0.85f),
    disabledOnSecondary = onSecondary.copy(alpha = 0.85f),
    disabledOnSecondaryVariant = onSurface.copy(alpha = 0.85f),
)

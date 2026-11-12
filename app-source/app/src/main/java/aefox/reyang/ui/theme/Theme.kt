package aefox.reyang.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode

enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    MONET_SYSTEM(3),
    MONET_LIGHT(4),
    MONET_DARK(5),
    DARK_AMOLED(6);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
    }

    val isSystem: Boolean get() = value == 0 || value == 3
    val isDark: Boolean get() = value == 2 || value == 5 || value == 6
    val isAmoled: Boolean get() = value == 6
    val isMonet: Boolean get() = value >= 3

    fun toNonMonetMode(): Int = when (this) {
        MONET_SYSTEM -> 0
        MONET_LIGHT -> 1
        MONET_DARK, DARK_AMOLED -> 2
        else -> value
    }

    fun toMonetMode(): Int = when (this) {
        SYSTEM -> 3
        LIGHT -> 4
        DARK -> 5
        else -> value
    }
}

data class AppSettings(
    val colorMode: ColorMode,
    val keyColor: Int,
    val paletteStyle: PaletteStyle,
    val colorSpec: ColorSpec.SpecVersion,
)

object ThemeController {
    fun getAppSettings(context: Context): AppSettings {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val uiMode = prefs.getString("ui_mode", UiMode.DEFAULT_VALUE) ?: UiMode.DEFAULT_VALUE
        var colorModeValue = prefs.getInt("color_mode", ColorMode.SYSTEM.value)

        if (uiMode == "miuix") {
            val miuixMonet = prefs.getBoolean("miuix_monet", false)
            val colorMode = ColorMode.fromValue(colorModeValue)
            colorModeValue = if (!miuixMonet && colorMode.isMonet) {
                colorMode.toNonMonetMode()
            } else if (miuixMonet && !colorMode.isMonet) {
                colorMode.toMonetMode()
            } else {
                colorModeValue
            }
        }

        val colorMode = ColorMode.fromValue(colorModeValue)
        val keyColor = prefs.getInt("key_color", 0)
        val paletteStyleStr = prefs.getString("color_style", PaletteStyle.TonalSpot.name)
        val paletteStyle = try {
            PaletteStyle.valueOf(paletteStyleStr!!)
        } catch (_: Exception) {
            PaletteStyle.TonalSpot
        }
        val colorSpecStr = prefs.getString("color_spec", ColorSpec.SpecVersion.Default.name)
        val colorSpec = try {
            ColorSpec.SpecVersion.valueOf(colorSpecStr!!)
        } catch (_: Exception) {
            ColorSpec.SpecVersion.Default
        }

        return AppSettings(colorMode, keyColor, paletteStyle, colorSpec)
    }
}

@Composable
fun TemplateTheme(
    appSettings: AppSettings? = null,
    uiMode: UiMode = LocalUiMode.current,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentAppSettings = appSettings ?: ThemeController.getAppSettings(context)

    when (uiMode) {
        UiMode.Miuix -> MiuixTemplateTheme(
            appSettings = currentAppSettings,
            content = content
        )

        UiMode.Material -> MaterialTemplateTheme(
            appSettings = currentAppSettings,
            content = content
        )
    }
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(): Boolean {
    return when (LocalColorMode.current) {
        1, 4 -> false  // Force light mode
        2, 5, 6 -> true   // Force dark mode
        else -> isSystemInDarkTheme()  // Follow system (0 or default)
    }
}


val LocalColorMode = staticCompositionLocalOf { 0 }

val LocalEnableBlur = staticCompositionLocalOf { false }

/** Progressive (edge gradient) blur behind the top and bottom bars (mirrors SukiSU). */
val LocalEnableProgressiveBlur = staticCompositionLocalOf { false }

val LocalEnableFloatingBottomBar = staticCompositionLocalOf { false }

val LocalEnableFloatingBottomBarBlur = staticCompositionLocalOf { false }

/** Whether the bottom bar/rail shows the installed supported app count badge. */
val LocalEnableNavigationBadge = staticCompositionLocalOf { true }

/**
 * Maximum description lines on the app list rows (theme settings → 界面细节).
 * Provided from the settings snapshot so the list screens can fold long text
 * exactly like the reference manager.
 */
val LocalModuleDescriptionMaxLines = staticCompositionLocalOf { 4 }

/**
 * Whether a theme package background image is active. Provided once from the
 * activity-level resolved background file; theme pipelines and card colors read
 * it to switch on the frosted-glass look (mirrors SukiSU LocalBackgroundImagePath).
 */
val LocalHasCustomBackground = staticCompositionLocalOf { false }

/**
 * Opacity applied to card/surface container colors while a theme package
 * background is active, so the wallpaper shows through blurred surfaces.
 * Mirrors SukiSU LocalCardBackgroundAlpha (default 0.6).
 */
val LocalCardBackgroundAlpha = staticCompositionLocalOf { 0.6f }

/**
 * Translucent variant of [color] while a theme package background is active;
 * unchanged otherwise. Component-level integration layer for containers that bypass the
 * scheme-level glass pass (SukiSU backgroundAwareColor).
 */
@Composable
fun backgroundAwareColor(
    color: Color,
    backgroundAlpha: Float = LocalCardBackgroundAlpha.current,
): Color {
    return if (LocalHasCustomBackground.current) {
        color.copy(alpha = backgroundAlpha)
    } else {
        color
    }
}

/**
 * Neutral bar surface: white in light mode, near-black in dark mode. Bottom bar
 * containers use it instead of the scheme surface/primary so a theme package
 * cannot tint the bar itself; icons, labels and the selection indicator still
 * follow the theme colors.
 */
@Composable
fun neutralBarContainerColor(): Color {
    return if (isInDarkTheme()) Color(0xFF161618) else Color.White
}

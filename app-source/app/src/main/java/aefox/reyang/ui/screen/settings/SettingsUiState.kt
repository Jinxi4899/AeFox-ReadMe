package aefox.reyang.ui.screen.settings

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.theme.ThemeVisuals

@Immutable
data class SettingsUiState(
    val uiMode: String = UiMode.DEFAULT_VALUE,
    val themeMode: Int = 0,
    val miuixMonet: Boolean = false,
    val keyColor: Int = 0,
    /** Derive the key color from the active background image instead of the picked color. */
    val extractColorFromBackground: Boolean = false,
    val colorStyle: String = PaletteStyle.TonalSpot.name,
    val colorSpec: String = ColorSpec.SpecVersion.Default.name,
    val enablePredictiveBack: Boolean = true,
    val enableSwipeDismiss: Boolean = true,
    val pagerInterceptionMode: Int = 1,
    val enableBlur: Boolean = true,
    /** Progressive blur switch (persisted only; the renderer does not consume it yet). */
    val enableProgressiveBlur: Boolean = false,
    val enableFloatingBottomBar: Boolean = true,
    val enableFloatingBottomBarBlur: Boolean = true,
    /** Show the installed supported app count as a navigation badge. */
    val enableNavigationBadge: Boolean = true,
    /** Persisted app language tag (empty = follow system). */
    val language: String = "",
    val pageScale: Float = 1.0f,
    /** Maximum description lines before a module card folds its text (1..5). */
    val moduleDescriptionMaxLines: Int = 4,
    /** Raw static appearance overlay JSON (empty when none); source for the overlay editor. */
    val themeAppearanceJson: String = "",
    /** Theme visuals snapshot: background path, card background, stickers, bottom bar icons, layout params, custom texts, etc. */
    val themeVisuals: ThemeVisuals = ThemeVisuals(),
    /** Active theme package id (appearance data change key). */
    val themeActiveId: String = "",
    /** Display name of the active theme package (empty when none). */
    val themeActiveName: String = "",
    /** Dark/light choice the current theme visuals apply (true = dark). */
    val isDarkTheme: Boolean = false,
    /** Whether a theme trial is in progress. */
    val themeTrialActive: Boolean = false,
    /** Adaptive builder: a light variant has been captured. */
    val adaptiveLightCaptured: Boolean = false,
    /** Adaptive builder: a dark variant has been captured. */
    val adaptiveDarkCaptured: Boolean = false,
    /** Applied adaptive variant mode (-1 none, 0 light, 1 dark). */
    val adaptiveAppliedMode: Int = -1,
    /** Selected launcher icon id (see aefox.reyang.ui.util.BuiltinAppIcons). */
    val appIconId: String = "default",
    /** Whether the selected launcher icon uses its dynamic-color variant. */
    val appIconDynamicColor: Boolean = false,
    /** The captured system wallpaper is the active global background. */
    val useSystemWallpaper: Boolean = false,
    /** A wallpaper capture is running (switch shows progress and ignores taps). */
    val systemWallpaperBusy: Boolean = false,
)

@Immutable
data class SettingsScreenActions(
    val onOpenTheme: () -> Unit,
    val onSetUiModeIndex: (Int) -> Unit,
    val onSetLanguage: (String) -> Unit,
    val onOpenAbout: () -> Unit,
)

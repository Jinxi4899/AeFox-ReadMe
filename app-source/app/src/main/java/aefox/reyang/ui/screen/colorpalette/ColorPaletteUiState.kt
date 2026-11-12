package aefox.reyang.ui.screen.colorpalette

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import aefox.reyang.ui.screen.settings.SettingsUiState
import aefox.reyang.ui.theme.ColorMode
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeCardStickerTransform
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment

@Immutable
data class ColorPaletteUiState(
    val uiState: SettingsUiState,
    val currentColorMode: ColorMode,
    val currentPaletteStyle: PaletteStyle,
    val currentColorSpec: ColorSpec.SpecVersion,
)

@Immutable
data class ColorPaletteScreenActions(
    val onBack: () -> Unit,
    val onSetThemeMode: (Int) -> Unit,
    val onSetMiuixMonet: (Boolean) -> Unit,
    val onSetKeyColor: (Int) -> Unit,
    val onSetColorMode: (ColorMode) -> Unit,
    val onSetColorStyle: (String) -> Unit,
    val onSetColorSpec: (String) -> Unit,
    val onSetEnableBlur: (Boolean) -> Unit,
    val onSetEnableProgressiveBlur: (Boolean) -> Unit,
    val onSetEnableFloatingBottomBar: (Boolean) -> Unit,
    val onSetEnableFloatingBottomBarBlur: (Boolean) -> Unit,
    val onSetEnableNavigationBadge: (Boolean) -> Unit,
    val onSetEnablePredictiveBack: (Boolean) -> Unit,
    val onSetEnableSwipeDismiss: (Boolean) -> Unit,
    val onSetPagerInterceptionMode: (Int) -> Unit,
    val onSetPageScale: (Float) -> Unit,

    // ---- Home card display (mirrors SukiSU's HOME_CARD category) ----
    val onSetHomeLayout: (HomeLayout) -> Unit,
    val onSetHomeStatusAlignment: (HomeStatusAlignment) -> Unit,
    val onSetHomeStatusScale: (Float) -> Unit,
    val onSetHomePillScale: (Float) -> Unit,
    val onSetHomeStatusWidthFraction: (Float) -> Unit,
    val onSetHideHomeCardStatusText: (Boolean) -> Unit,
    val onSetHideHomeCardDecoration: (Boolean) -> Unit,
    val onSetHomeCardImageStyle: (HomeCardImageStyle) -> Unit,
    val onSetShowHomeCardImageWithoutBackground: (Boolean) -> Unit,
    val onPickHomeCardImage: () -> Unit,
    val onClearHomeCardImage: () -> Unit,
    val onPickHomeCardSticker: () -> Unit,
    val onRemoveHomeCardSticker: (Int) -> Unit,
    val onUpdateHomeCardStickerTransform: (Int, HomeCardStickerTransform) -> Unit,

    // ---- Global background image (mirrors SukiSU's BACKGROUND category) ----
    val onPickBackgroundImage: () -> Unit,
    /** Re-open the crop screen with the current custom background. */
    val onCropCurrentBackground: () -> Unit,
    val onClearBackgroundImage: () -> Unit,
    val onSetUseSystemWallpaper: (Boolean) -> Unit,
    val onSetExtractColorFromBackground: (Boolean) -> Unit,
    val onSetCardBackgroundAlphaPercent: (Int) -> Unit,
    val onSetCustomBackgroundBlurPercent: (Int) -> Unit,
    val onSetCustomBackgroundScrimPercent: (Int) -> Unit,

    // ---- Custom bottom bar icons (slot key from BottomBarDestination.slotKey) ----
    val onPickBottomBarIcon: (String) -> Unit,
    val onClearBottomBarIcon: (String) -> Unit,

    // ---- Custom texts + appearance overlay + details ----
    val onOpenCustomTexts: () -> Unit,
    val onOpenAppearanceOverlay: () -> Unit,
    val onSetModuleDescriptionMaxLines: (Int) -> Unit,

    // ---- Appearance-wide operations ----
    val onResetBackgroundAppearance: () -> Unit,
    val onClearAllAppearance: () -> Unit,

    // ---- Theme package actions (Task 6.5: import/export/restore default/cancel trial) ----
    val onImportTheme: () -> Unit,
    val onExportTheme: () -> Unit,
    val onRestoreDefault: () -> Unit,
    val onCancelTrial: () -> Unit,
    /** Opens the launcher icon picker. */
    val onOpenAppIcon: () -> Unit,
)

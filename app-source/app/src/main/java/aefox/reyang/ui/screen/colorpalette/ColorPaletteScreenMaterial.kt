package aefox.reyang.ui.screen.colorpalette

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.roundToInt
import aefox.reyang.R
import aefox.reyang.ui.component.HomeCardStickerStage
import aefox.reyang.ui.component.HomeCardStickerThumbnail
import aefox.reyang.ui.component.bottombar.BottomBarDestination
import aefox.reyang.ui.component.material.ExpressiveScaffold
import aefox.reyang.ui.component.material.LocalListItemShapes
import aefox.reyang.ui.component.material.SegmentedColumn
import aefox.reyang.ui.component.material.SegmentedDropdownItem
import aefox.reyang.ui.component.material.SegmentedListItem
import aefox.reyang.ui.component.material.SegmentedSwitchItem
import aefox.reyang.ui.component.material.TonalCard
import aefox.reyang.ui.component.material.TopBarBackButton
import aefox.reyang.ui.component.material.expressiveTopAppBarColors
import aefox.reyang.ui.screen.home.HomeStatusUi
import aefox.reyang.ui.theme.ColorMode
import aefox.reyang.ui.theme.ThemeVisuals
import aefox.reyang.ui.theme.isInDarkTheme
import aefox.reyang.ui.theme.keyColorOptions
import aefox.reyang.ui.theme.rememberThemeHomeCardStickers
import aefox.reyang.ui.theme.themeBackgroundFile
import aefox.reyang.ui.theme.themeCustomText
import aefox.reyang.ui.theme.themeHomeCardImageFile
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeCardSticker
import aefox.reyang.ui.theme.themepackage.HomeCardStickerTransform
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle

@Composable
fun ColorPaletteScreenMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val uiState = state.uiState
    val currentColorMode = state.currentColorMode
    val colorStyle = state.currentPaletteStyle
    val colorSpec = state.currentColorSpec
    val context = LocalContext.current
    val isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme()
    val pinnedHeader = LocalConfiguration.current.screenHeightDp >= 600

    var pageScaleDraft by remember { mutableFloatStateOf(uiState.pageScale) }
    val visuals = uiState.themeVisuals
    val backgroundPath = themeBackgroundFile(context, visuals.adaptive, isDark)?.absolutePath.orEmpty()
    val previewSpec = remember(
        uiState.keyColor,
        isDark,
        currentColorMode.isAmoled,
        colorStyle,
        colorSpec,
        backgroundPath,
        visuals.backgroundBlurPercent,
        visuals.backgroundScrimPercent,
        uiState.enableFloatingBottomBar,
        pageScaleDraft,
        visuals.appearance,
        uiState.themeActiveId,
    ) {
        ThemePreviewSpec(
            keyColor = uiState.keyColor,
            isDark = isDark,
            isAmoled = currentColorMode.isAmoled,
            paletteStyle = colorStyle,
            colorSpec = colorSpec,
            backgroundPath = backgroundPath,
            backgroundBlurPercent = visuals.backgroundBlurPercent,
            backgroundScrimPercent = visuals.backgroundScrimPercent,
            floatingBottomBar = uiState.enableFloatingBottomBar,
            pageScale = pageScaleDraft,
            appearance = visuals.appearance,
        )
    }
    val modeLabel = stringResource(
        when {
            currentColorMode.isSystem -> R.string.settings_theme_mode_system
            currentColorMode.isDark -> R.string.settings_theme_mode_dark
            else -> R.string.settings_theme_mode_light
        }
    ) + " · " + colorStyle.name
    var selectedCategoryKey by rememberSaveable { mutableStateOf(ThemeSettingsCategory.COLORS.name) }
    // Fullscreen live preview: the compact card opens the two-style pager viewer.
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    val selectedCategory = ThemeSettingsCategory.entries
        .firstOrNull { it.name == selectedCategoryKey }
        ?: ThemeSettingsCategory.COLORS

    ExpressiveScaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TopBarBackButton(onClick = actions.onBack)
                },
                title = { Text(themeCustomText("page.appearance", stringResource(R.string.settings_theme))) },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { paddingValues ->
        val navBars = WindowInsets.navigationBars.asPaddingValues()
        val captionBar = WindowInsets.captionBar.asPaddingValues()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (pinnedHeader) {
                MaterialCompactPreviewCard(
                    spec = previewSpec,
                    modeLabel = modeLabel,
                    onClick = { showFullPreview = true },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                )
                MaterialCategoryGrid(
                    selected = selectedCategory,
                    onSelect = { selectedCategoryKey = it.name },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                contentPadding = PaddingValues(top = 13.dp),
            ) {
                if (!pinnedHeader) {
                    item {
                        MaterialCompactPreviewCard(
                            spec = previewSpec,
                            modeLabel = modeLabel,
                            onClick = { showFullPreview = true },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    item {
                        MaterialCategoryGrid(
                            selected = selectedCategory,
                            onSelect = { selectedCategoryKey = it.name },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                if (selectedCategory == ThemeSettingsCategory.COLORS) {
                    item { ColorsSectionMaterial(state, actions) }
                }
                if (selectedCategory == ThemeSettingsCategory.BACKGROUND) {
                    item { BackgroundSectionMaterial(state, actions) }
                }
                if (selectedCategory == ThemeSettingsCategory.ICON_TEXT) {
                    item { IconTextSectionMaterial(actions) }
                }
                if (selectedCategory == ThemeSettingsCategory.HOME_CARD) {
                    item { HomeCardSectionMaterial(state, actions) }
                }
                if (selectedCategory == ThemeSettingsCategory.BOTTOM_BAR) {
                    item { BottomBarSectionMaterial(state, actions) }
                }
                if (selectedCategory == ThemeSettingsCategory.INTERACTION) {
                    item {
                        InteractionSectionMaterial(
                            state = state,
                            actions = actions,
                            pageScaleDraft = pageScaleDraft,
                            onPageScaleCommit = { pageScaleDraft = it },
                        )
                    }
                }
                if (selectedCategory == ThemeSettingsCategory.DETAILS) {
                    item { DetailsSectionMaterial(state, actions) }
                }
                if (selectedCategory == ThemeSettingsCategory.THEME_PACKAGE) {
                    item { ThemePackageSectionMaterial(state, actions) }
                }
                item {
                    Spacer(
                        modifier = Modifier.height(
                            16.dp + navBars.calculateBottomPadding() + captionBar.calculateBottomPadding()
                        )
                    )
                }
            }
        }
        // Fullscreen preview: page 0 renders the Material style, page 1 the Miuix style
        // of the same live spec (mirrors SukiSU).
        ThemeFullscreenPreviewDialog(
            show = showFullPreview,
            onDismiss = { showFullPreview = false },
        ) { previewModifier, page ->
            if (page == 0) {
                ThemePreviewMaterial(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            } else {
                ThemePreviewMiuix(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            }
        }
    }
}

/** Pinned compact preview: thumbnail device frame + style caption + palette swatches. */
@Composable
private fun MaterialCompactPreviewCard(
    spec: ThemePreviewSpec,
    modeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    TonalCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemePreviewMaterial(
                spec = spec,
                modifier = Modifier.width(88.dp),
                compact = true,
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_preview_caption_material),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = colors.onSurface,
                )
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                ThemePaletteSwatches(
                    colors = listOf(
                        colors.primary,
                        colors.secondaryContainer,
                        colors.tertiaryContainer,
                        colors.surfaceContainerHighest,
                    ),
                )
                Text(
                    text = stringResource(R.string.theme_preview_compact_summary),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** The category box: only one category's settings are composed at a time. */
@Composable
private fun MaterialCategoryGrid(
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            ThemeSettingsCategory.entries.chunked(4).forEach { row ->
                CategoryGridRow(row, selected, onSelect)
            }
        }
    }
}

@Composable
private fun CategoryGridRow(
    row: List<ThemeSettingsCategory>,
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        row.forEachIndexed { index, category ->
            CategoryGridTile(category, category == selected, index, row.lastIndex, Modifier.weight(1f), onSelect)
        }
    }
}

@Composable
private fun CategoryGridTile(
    category: ThemeSettingsCategory,
    selected: Boolean,
    index: Int,
    lastIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (ThemeSettingsCategory) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    ToggleButton(
        checked = selected,
        onCheckedChange = { checked ->
            if (checked) {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onSelect(category)
            }
        },
        shapes = when (index) {
            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
            lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
        },
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
        modifier = modifier,
    ) {
        CategoryTileContent(category)
    }
}

@Composable
private fun CategoryTileContent(category: ThemeSettingsCategory) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Segmented list row that hosts custom content (mirrors SukiSU SegmentedItemContainer). */
@Composable
private fun SegmentedBoxItem(content: @Composable () -> Unit) {
    val shape = LocalListItemShapes.current?.shape ?: RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    ) {
        content()
    }
}

/** Segmented category header row (title + summary + icon tile). */
@Composable
private fun MaterialCategoryHeader(category: ThemeSettingsCategory) {
    val title = stringResource(category.titleRes)
    SegmentedListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(stringResource(category.summaryRes)) },
        leadingContent = { MaterialIconTile(category.icon, title) },
    )
}

/** COLORS: theme mode button group, key color swatches, palette style, spec and overlay. */
@Composable
private fun ColorsSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.COLORS) }
        add {
            SegmentedBoxItem {
                ThemeModeButtonGroup(state, actions)
            }
        }
        add {
            SegmentedBoxItem {
                KeyColorSwatchRow(state, actions)
            }
        }
        add {
            val styles = PaletteStyle.entries
            SegmentedDropdownItem(
                icon = Icons.Rounded.Style,
                title = stringResource(R.string.settings_color_style),
                items = styles.map { it.name },
                selectedIndex = styles.indexOf(state.currentPaletteStyle),
                onItemSelected = { index ->
                    actions.onSetColorStyle(styles[index].name)
                }
            )
        }
        add {
            val specs = ColorSpec.SpecVersion.entries
            SegmentedDropdownItem(
                icon = Icons.Rounded.DesignServices,
                title = stringResource(R.string.settings_color_spec),
                items = specs.map { it.name },
                selectedIndex = specs.indexOf(state.currentColorSpec).coerceAtLeast(0),
                onItemSelected = { index ->
                    actions.onSetColorSpec(specs[index].name)
                }
            )
        }
        add {
            SegmentedListItem(
                onClick = actions.onOpenAppearanceOverlay,
                headlineContent = { Text(stringResource(R.string.appearance_overlay_title)) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (state.uiState.themeAppearanceJson.isNotBlank()) {
                                R.string.appearance_overlay_active
                            } else {
                                R.string.appearance_overlay_summary
                            }
                        )
                    )
                },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.Palette, stringResource(R.string.appearance_overlay_title))
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

/** Connected button group for the theme mode (system / light / dark / amoled). */
@Composable
private fun ThemeModeButtonGroup(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val currentColorMode = state.currentColorMode
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val options = listOf(
            listOf(ColorMode.SYSTEM) to stringResource(R.string.settings_theme_mode_system),
            listOf(ColorMode.LIGHT) to stringResource(R.string.settings_theme_mode_light),
            listOf(ColorMode.DARK) to stringResource(R.string.settings_theme_mode_dark),
            listOf(ColorMode.DARK_AMOLED) to stringResource(R.string.settings_theme_mode_dark),
        )
        options.chunked(4).forEach { rowOptions ->
            ThemeModeButtonRow(rowOptions, currentColorMode, actions)
        }
    }
}

@Composable
private fun ThemeModeButtonRow(
    rowOptions: List<Pair<List<ColorMode>, String>>,
    currentColorMode: ColorMode,
    actions: ColorPaletteScreenActions,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        rowOptions.forEachIndexed { index, (modes, label) ->
            ToggleButton(
                checked = currentColorMode in modes,
                onCheckedChange = {
                    if (it) {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        actions.onSetColorMode(modes.first())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    rowOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Icon(
                    imageVector = colorModeIcon(modes.first()),
                    contentDescription = label,
                )
            }
        }
    }
}

private fun colorModeIcon(mode: ColorMode): ImageVector = when (mode) {
    ColorMode.SYSTEM, ColorMode.MONET_SYSTEM -> Icons.Filled.Brightness4
    ColorMode.LIGHT, ColorMode.MONET_LIGHT -> Icons.Filled.Brightness7
    ColorMode.DARK, ColorMode.MONET_DARK -> Icons.Filled.Brightness3
    ColorMode.DARK_AMOLED -> Icons.Filled.Brightness1
}

/** Horizontally scrolling key color swatches (mirrors SukiSU's swatch row). */
@Composable
private fun KeyColorSwatchRow(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val isDark = state.currentColorMode.isDark ||
        state.currentColorMode.isSystem && isSystemInDarkTheme()
    // A background-derived seed is not one of the fixed options, so it shows as the default pick.
    val currentKeyColor = if (state.uiState.extractColorFromBackground) 0 else state.uiState.keyColor
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ColorButtonMaterial(
                color = Color.Unspecified,
                isSelected = currentKeyColor == 0,
                isDark = isDark,
                paletteStyle = state.currentPaletteStyle,
                colorSpec = state.currentColorSpec,
                onClick = { actions.onSetKeyColor(0) },
            )
        }
        items(keyColorOptions) { color ->
            ColorButtonMaterial(
                color = Color(color),
                isSelected = currentKeyColor == color,
                isDark = isDark,
                paletteStyle = state.currentPaletteStyle,
                colorSpec = state.currentColorSpec,
                onClick = { actions.onSetKeyColor(color) },
            )
        }
    }
}

/** BACKGROUND: custom image, re-crop, system wallpaper, color extraction and the appearance sliders. */
@Composable
private fun BackgroundSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val visuals = uiState.themeVisuals
    val backgroundPath = themeBackgroundFile(context, visuals.adaptive, dark)?.absolutePath.orEmpty()
    val showBackgroundAppearance = uiState.useSystemWallpaper || backgroundPath.isNotBlank()
    var backgroundAppearanceExpanded by rememberSaveable { mutableStateOf(true) }
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.BACKGROUND) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add {
                SegmentedSwitchItem(
                    icon = Icons.Rounded.BlurOn,
                    title = stringResource(R.string.settings_progressive_blur),
                    summary = stringResource(R.string.settings_progressive_blur_summary),
                    checked = uiState.enableProgressiveBlur,
                    onCheckedChange = actions.onSetEnableProgressiveBlur,
                )
            }
        }
        if (!uiState.useSystemWallpaper) {
            add {
                SegmentedListItem(
                    onClick = actions.onPickBackgroundImage,
                    headlineContent = { Text(stringResource(R.string.settings_custom_background)) },
                    supportingContent = { Text(stringResource(R.string.settings_custom_background_summary)) },
                    leadingContent = {
                        MaterialIconTile(Icons.Rounded.Image, stringResource(R.string.settings_custom_background))
                    },
                    trailingContent = if (backgroundPath.isNotBlank()) {
                        {
                            TextButton(onClick = actions.onClearBackgroundImage) {
                                Text(stringResource(R.string.settings_clear))
                            }
                        }
                    } else {
                        null
                    },
                )
            }
            if (backgroundPath.isNotBlank()) {
                add {
                    SegmentedListItem(
                        onClick = actions.onCropCurrentBackground,
                        headlineContent = { Text(stringResource(R.string.settings_crop_background)) },
                        supportingContent = { Text(stringResource(R.string.settings_crop_background_summary)) },
                        leadingContent = {
                            MaterialIconTile(Icons.Rounded.Crop, stringResource(R.string.settings_crop_background))
                        },
                    )
                }
            }
        }
        add {
            SegmentedSwitchItem(
                icon = Icons.Rounded.Wallpaper,
                title = stringResource(R.string.background_system_wallpaper),
                summary = stringResource(R.string.background_system_wallpaper_summary),
                checked = uiState.useSystemWallpaper,
                onCheckedChange = actions.onSetUseSystemWallpaper,
            )
        }
        if (showBackgroundAppearance) {
            add {
                SegmentedSwitchItem(
                    icon = Icons.Rounded.Colorize,
                    title = stringResource(R.string.settings_extract_background_color),
                    summary = stringResource(
                        if (uiState.miuixMonet) {
                            R.string.settings_extract_background_color_summary
                        } else {
                            R.string.settings_extract_background_color_disabled
                        }
                    ),
                    checked = uiState.extractColorFromBackground,
                    onCheckedChange = actions.onSetExtractColorFromBackground,
                )
            }
            add {
                SegmentedListItem(
                    onClick = { backgroundAppearanceExpanded = !backgroundAppearanceExpanded },
                    headlineContent = { Text(stringResource(R.string.settings_background_appearance)) },
                    supportingContent = { Text(stringResource(R.string.settings_background_appearance_summary)) },
                    leadingContent = {
                        MaterialIconTile(
                            Icons.Rounded.Colorize,
                            stringResource(R.string.settings_background_appearance),
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = if (backgroundAppearanceExpanded) {
                                Icons.Rounded.KeyboardArrowUp
                            } else {
                                Icons.Rounded.KeyboardArrowDown
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
            if (backgroundAppearanceExpanded) {
                add {
                    MaterialPercentSliderItem(
                        icon = Icons.Rounded.Contrast,
                        title = stringResource(R.string.settings_card_background_alpha),
                        summaryRes = R.string.settings_card_background_alpha_summary,
                        value = visuals.cardBackgroundAlphaPercent,
                        valueRange = 10f..91f,
                        onValueChangeFinished = actions.onSetCardBackgroundAlphaPercent,
                    )
                }
                add {
                    MaterialPercentSliderItem(
                        icon = Icons.Rounded.BlurOn,
                        title = stringResource(R.string.settings_background_blur),
                        summaryRes = R.string.settings_background_blur_summary,
                        value = visuals.backgroundBlurPercent,
                        valueRange = 0f..100f,
                        onValueChangeFinished = actions.onSetCustomBackgroundBlurPercent,
                    )
                }
                add {
                    MaterialPercentSliderItem(
                        icon = Icons.Rounded.Contrast,
                        title = stringResource(R.string.settings_background_scrim),
                        summaryRes = R.string.settings_background_scrim_summary,
                        value = visuals.backgroundScrimPercent,
                        valueRange = -100f..100f,
                        onValueChangeFinished = actions.onSetCustomBackgroundScrimPercent,
                        startLabel = stringResource(R.string.settings_background_scrim_darker),
                        endLabel = stringResource(R.string.settings_background_scrim_lighter),
                    )
                }
            }
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

/** Inline percent slider row for the theme percent fields (blur 0..100, scrim -100..100). */
@Composable
private fun MaterialPercentSliderItem(
    icon: ImageVector,
    title: String,
    @StringRes summaryRes: Int,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Int) -> Unit,
    startLabel: String? = null,
    endLabel: String? = null,
) {
    SegmentedBoxItem {
        val sliderState = rememberSliderState(value = value.toFloat(), trackRange = valueRange)
        LaunchedEffect(value) { sliderState.value = value.toFloat() }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(summaryRes, sliderState.value.roundToInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Slider(
                state = sliderState,
                onValueChangeFinished = {
                    onValueChangeFinished(sliderState.value.roundToInt())
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (startLabel != null && endLabel != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = startLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = endLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** BOTTOM_BAR: the four tab icon slot rows, blur, floating bar and its liquid glass effect. */
@Composable
private fun BottomBarSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.BOTTOM_BAR) }
        addBottomBarIconSlotRowsMaterial(this, uiState.themeVisuals.bottomBarIcons, actions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add {
                SegmentedSwitchItem(
                    icon = Icons.Rounded.BlurOn,
                    title = stringResource(id = R.string.settings_enable_blur),
                    summary = stringResource(id = R.string.settings_enable_blur_summary),
                    checked = uiState.enableBlur,
                    onCheckedChange = actions.onSetEnableBlur,
                )
            }
        }
        add {
            SegmentedSwitchItem(
                icon = Icons.Rounded.CallToAction,
                title = stringResource(id = R.string.settings_floating_bottom_bar),
                summary = stringResource(id = R.string.settings_floating_bottom_bar_summary),
                checked = uiState.enableFloatingBottomBar,
                onCheckedChange = actions.onSetEnableFloatingBottomBar,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add {
                SegmentedSwitchItem(
                    icon = Icons.Rounded.WaterDrop,
                    title = stringResource(id = R.string.settings_enable_glass),
                    summary = stringResource(id = R.string.settings_enable_glass_summary),
                    checked = uiState.enableFloatingBottomBarBlur,
                    onCheckedChange = actions.onSetEnableFloatingBottomBarBlur,
                )
            }
        }
        add {
            SegmentedSwitchItem(
                icon = Icons.Rounded.Pin,
                title = stringResource(id = R.string.settings_navigation_badge),
                summary = stringResource(id = R.string.settings_navigation_badge_summary),
                checked = uiState.enableNavigationBadge,
                onCheckedChange = actions.onSetEnableNavigationBadge,
            )
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

/** Appends the four tab icon slot rows: preview + pick + clear (mirrors SukiSU's slot rows). */
private fun addBottomBarIconSlotRowsMaterial(
    builder: MutableList<@Composable () -> Unit>,
    iconPaths: Map<String, String>,
    actions: ColorPaletteScreenActions,
) {
    BottomBarDestination.entries.forEach { destination ->
        val path = iconPaths[destination.slotKey]?.takeIf { it.isNotBlank() }
        builder.add {
            val label = stringResource(destination.label)
            SegmentedListItem(
                onClick = { actions.onPickBottomBarIcon(destination.slotKey) },
                headlineContent = { Text(label) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (path != null) {
                                R.string.settings_bottom_bar_icon_custom_summary
                            } else {
                                R.string.settings_bottom_bar_icon_default_summary
                            }
                        )
                    )
                },
                leadingContent = { BottomBarSlotPreviewMaterial(path, destination.icon, label) },
                trailingContent = if (path != null) {
                    {
                        TextButton(onClick = { actions.onClearBottomBarIcon(destination.slotKey) }) {
                            Text(stringResource(R.string.settings_clear))
                        }
                    }
                } else {
                    null
                },
            )
        }
    }
}

/** Slot leading preview: the custom image when set, otherwise the built-in tab icon tile. */
@Composable
private fun BottomBarSlotPreviewMaterial(path: String?, icon: ImageVector, contentDescription: String) {
    if (path != null) {
        AsyncImage(
            model = File(path),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    } else {
        MaterialIconTile(icon, contentDescription)
    }
}

/** INTERACTION: predictive back, swipe dismiss, pager gestures and page scale. */
@Composable
private fun InteractionSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    pageScaleDraft: Float,
    onPageScaleCommit: (Float) -> Unit,
) {
    val uiState = state.uiState
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.INTERACTION) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add {
                SegmentedSwitchItem(
                    icon = Icons.AutoMirrored.Rounded.MenuOpen,
                    title = stringResource(id = R.string.settings_enable_predictive_back),
                    summary = stringResource(id = R.string.settings_enable_predictive_back_summary),
                    checked = uiState.enablePredictiveBack,
                    onCheckedChange = actions.onSetEnablePredictiveBack,
                )
            }
        }
        add {
            SegmentedSwitchItem(
                icon = Icons.Rounded.Swipe,
                title = stringResource(R.string.settings_enable_swipe_dismiss),
                summary = stringResource(R.string.settings_enable_swipe_dismiss_summary),
                checked = uiState.enableSwipeDismiss,
                onCheckedChange = actions.onSetEnableSwipeDismiss,
            )
        }
        add {
            SegmentedDropdownItem(
                icon = Icons.Rounded.ViewCarousel,
                title = stringResource(R.string.settings_pager_gesture_mode),
                items = listOf(
                    stringResource(R.string.settings_pager_gesture_native),
                    stringResource(R.string.settings_pager_gesture_cross_axis),
                    stringResource(R.string.settings_pager_gesture_ios_like),
                ),
                selectedIndex = uiState.pagerInterceptionMode,
                onItemSelected = actions.onSetPagerInterceptionMode,
            )
        }
        add {
            PageScaleSliderItem(pageScaleDraft, onPageScaleCommit, actions)
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

@Composable
private fun PageScaleSliderItem(
    pageScaleDraft: Float,
    onPageScaleCommit: (Float) -> Unit,
    actions: ColorPaletteScreenActions,
) {
    SegmentedBoxItem {
        val sliderState = rememberSliderState(
            value = pageScaleDraft,
            trackRange = 0.8f..1.1f,
        )
        LaunchedEffect(pageScaleDraft) {
            sliderState.value = pageScaleDraft
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.AspectRatio,
                    contentDescription = stringResource(id = R.string.settings_page_scale),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_page_scale),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.settings_page_scale_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${(sliderState.value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                state = sliderState,
                onValueChangeFinished = {
                    onPageScaleCommit(sliderState.value)
                    actions.onSetPageScale(sliderState.value)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * THEME_PACKAGE: import / export / reset / clear-all in the primary group, with the
 * active theme, trial controls, adaptive builder, app icon and restore-default in a
 * trailing group (mirrors SukiSU's block layout).
 */
@Composable
private fun ThemePackageSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    val showResetDialog = rememberSaveable { mutableStateOf(false) }
    val showClearAllDialog = rememberSaveable { mutableStateOf(false) }
    val primary: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.THEME_PACKAGE) }
        add {
            SegmentedListItem(
                onClick = actions.onImportTheme,
                headlineContent = { Text(stringResource(R.string.theme_pkg_import_local)) },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.FileDownload, stringResource(R.string.theme_pkg_import_local))
                },
            )
        }
        add {
            SegmentedListItem(
                onClick = actions.onExportTheme,
                headlineContent = { Text(stringResource(R.string.theme_pkg_export_current)) },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.FileUpload, stringResource(R.string.theme_pkg_export_current))
                },
            )
        }
        add {
            SegmentedListItem(
                onClick = { showResetDialog.value = true },
                headlineContent = { Text(stringResource(R.string.theme_reset)) },
                supportingContent = { Text(stringResource(R.string.theme_reset_summary)) },
                leadingContent = { MaterialIconTile(Icons.Rounded.Restore, stringResource(R.string.theme_reset)) },
            )
        }
        add {
            SegmentedListItem(
                onClick = { showClearAllDialog.value = true },
                headlineContent = { Text(stringResource(R.string.theme_clear_all)) },
                supportingContent = { Text(stringResource(R.string.theme_clear_all_summary)) },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.Delete, stringResource(R.string.theme_clear_all))
                },
            )
        }
    }
    val extras: List<@Composable () -> Unit> = buildList {
        add {
            SegmentedListItem(
                headlineContent = { Text(stringResource(R.string.theme_pkg_current_theme)) },
                supportingContent = {
                    Text(
                        if (uiState.themeActiveName.isBlank()) {
                            stringResource(R.string.theme_pkg_no_theme)
                        } else {
                            uiState.themeActiveName
                        },
                    )
                },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.Palette, stringResource(R.string.theme_pkg_current_theme))
                },
            )
        }
        if (uiState.themeTrialActive) {
            add {
                SegmentedListItem(
                    headlineContent = {
                        Text(stringResource(R.string.theme_pkg_trial_banner, uiState.themeActiveName))
                    },
                    trailingContent = {
                        TextButton(onClick = actions.onCancelTrial) {
                            Text(stringResource(R.string.theme_pkg_cancel_trial))
                        }
                    },
                )
            }
        }
        add {
            SegmentedListItem(
                onClick = actions.onRestoreDefault,
                headlineContent = { Text(stringResource(R.string.theme_pkg_restore_default)) },
                supportingContent = { Text(stringResource(R.string.theme_pkg_restore_default_body)) },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.Restore, stringResource(R.string.theme_pkg_restore_default))
                },
            )
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = primary)
    SegmentedColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        content = extras,
    )
    ThemeConfirmDialogMaterial(
        show = showResetDialog.value,
        title = stringResource(R.string.theme_reset_confirm_title),
        message = stringResource(R.string.theme_reset_confirm_message),
        confirmText = stringResource(R.string.theme_reset),
        onDismiss = { showResetDialog.value = false },
        onConfirm = {
            showResetDialog.value = false
            actions.onResetBackgroundAppearance()
        },
    )
    ThemeConfirmDialogMaterial(
        show = showClearAllDialog.value,
        title = stringResource(R.string.theme_clear_all_confirm_title),
        message = stringResource(R.string.theme_clear_all_confirm_message),
        confirmText = stringResource(R.string.theme_clear_all),
        onDismiss = { showClearAllDialog.value = false },
        onConfirm = {
            showClearAllDialog.value = false
            actions.onClearAllAppearance()
        },
    )
}

@Composable
private fun ColorButtonMaterial(
    color: Color,
    isSelected: Boolean,
    isDark: Boolean,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.Default,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val colorScheme = if (color == Color.Unspecified) {
        val baseScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        rememberDynamicColorScheme(
            seedColor = Color.Unspecified,
            isDark = isDark,
            style = paletteStyle,
            specVersion = colorSpec,
            primary = baseScheme.primary,
            secondary = baseScheme.secondary,
            tertiary = baseScheme.tertiary,
            neutral = baseScheme.surface,
            neutralVariant = baseScheme.surfaceVariant,
            error = baseScheme.error,
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = color,
            isDark = isDark,
            style = paletteStyle,
            specVersion = colorSpec,
        )
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainer,
        modifier = Modifier.size(72.dp),
    ) {
        ColorButtonSwatch(colorScheme, isSelected)
    }
}

/** Arc swatch + selection ring of a key color button. */
@Composable
private fun ColorButtonSwatch(
    colorScheme: ColorScheme,
    isSelected: Boolean,
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(48.dp)) {
            drawArc(
                color = colorScheme.primaryContainer,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
            )
            drawArc(
                color = colorScheme.tertiaryContainer,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
            )
        }

        val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f)
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(16.dp),
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = !isSelected,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(colorScheme.primary, CircleShape),
                )
            }
        }
    }
}

/** Shared confirm dialog for the appearance-wide reset/clear operations (Material). */
@Composable
private fun ThemeConfirmDialogMaterial(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )
}

/** ICON_TEXT: launcher icon picker and the custom interface texts entry. */
@Composable
private fun IconTextSectionMaterial(
    actions: ColorPaletteScreenActions,
) {
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.ICON_TEXT) }
        add {
            SegmentedListItem(
                onClick = actions.onOpenAppIcon,
                headlineContent = { Text(stringResource(R.string.app_icon_title)) },
                supportingContent = { Text(stringResource(R.string.app_icon_summary)) },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.AppShortcut, stringResource(R.string.app_icon_title))
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
        add {
            SegmentedListItem(
                onClick = actions.onOpenCustomTexts,
                headlineContent = { Text(stringResource(R.string.settings_custom_texts)) },
                supportingContent = { Text(stringResource(R.string.settings_custom_texts_summary)) },
                leadingContent = {
                    MaterialIconTile(Icons.Rounded.Edit, stringResource(R.string.settings_custom_texts))
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

/** A labeled text field: label above, input below (avoids the floating-label overlap). */
@Composable
internal fun MaterialTextFieldRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

/** HOME_CARD: layout, card image, immersive display options, sliders and sticker editor. */
@Composable
private fun HomeCardSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val visuals = state.uiState.themeVisuals
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val cardImagePath = themeHomeCardImageFile(context, visuals.adaptive, dark)?.absolutePath.orEmpty()
    val backgroundPath = themeBackgroundFile(context, visuals.adaptive, dark)?.absolutePath.orEmpty()
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.HOME_CARD) }
        add {
            SegmentedBoxItem {
                HomeLayoutButtonGroupMaterial(visuals.homeLayout, actions)
            }
        }
        addCardImageRowMaterial(this, state, actions, cardImagePath)
        addClassicOptionsMaterial(this, state, actions, cardImagePath)
        if (visuals.homeLayout == HomeLayout.IMMERSIVE) {
            addImmersiveOptionsMaterial(this, state, actions, cardImagePath, backgroundPath)
        }
        addStickerRowsMaterial(this, state, actions, cardImagePath, backgroundPath)
        addClearImageRowMaterial(this, actions, cardImagePath)
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

/** Card background image row: summary reflects whether an image is set. */
private fun addCardImageRowMaterial(
    builder: MutableList<@Composable () -> Unit>,
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    builder.add {
        SegmentedListItem(
            onClick = actions.onPickHomeCardImage,
            headlineContent = { Text(stringResource(R.string.settings_home_card_image)) },
            supportingContent = {
                Text(
                    stringResource(
                        if (cardImagePath.isBlank()) {
                            R.string.settings_home_card_image_default
                        } else {
                            R.string.settings_home_card_image_set
                        }
                    )
                )
            },
            leadingContent = {
                MaterialIconTile(Icons.Rounded.Image, stringResource(R.string.settings_home_card_image))
            },
        )
    }
}

/** Options only visible for the classic layout with a card image. */
private fun addClassicOptionsMaterial(
    builder: MutableList<@Composable () -> Unit>,
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    val visuals = state.uiState.themeVisuals
    if (visuals.homeLayout != HomeLayout.CLASSIC || cardImagePath.isBlank()) return
    builder.add {
        SegmentedSwitchItem(
            icon = Icons.Rounded.Star,
            title = stringResource(R.string.settings_hide_home_card_decoration),
            summary = stringResource(R.string.settings_hide_home_card_decoration_summary),
            checked = visuals.hideHomeCardDecoration,
            onCheckedChange = actions.onSetHideHomeCardDecoration,
        )
    }
    builder.add {
        SegmentedSwitchItem(
            icon = Icons.Rounded.Home,
            title = stringResource(R.string.settings_hide_home_card_status_text),
            summary = stringResource(R.string.settings_hide_home_card_status_text_summary),
            checked = visuals.hideHomeCardStatusText,
            onCheckedChange = actions.onSetHideHomeCardStatusText,
        )
    }
}

/** Sticker editor entry (or the "hidden" notice) + remove-card-background row. */
private fun addStickerRowsMaterial(
    builder: MutableList<@Composable () -> Unit>,
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
    backgroundPath: String,
) {
    val visuals = state.uiState.themeVisuals
    when {
        canUseStickersMaterial(visuals, cardImagePath, backgroundPath) ->
            builder.add { StickerEditorMaterial(state, actions, cardImagePath) }

        visuals.homeLayout == HomeLayout.IMMERSIVE &&
            visuals.homeCardImageStyle == HomeCardImageStyle.BANNER ->
            builder.add { StickerHiddenRowMaterial() }
    }
}

private fun addClearImageRowMaterial(
    builder: MutableList<@Composable () -> Unit>,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    if (cardImagePath.isBlank()) return
    builder.add {
        SegmentedListItem(
            onClick = actions.onClearHomeCardImage,
            headlineContent = { Text(stringResource(R.string.settings_home_card_image_clear)) },
            supportingContent = {
                Text(stringResource(R.string.settings_home_card_image_clear_summary))
            },
            leadingContent = {
                MaterialIconTile(
                    Icons.Rounded.Restore,
                    stringResource(R.string.settings_home_card_image_clear),
                )
            },
        )
    }
}

@Composable
private fun StickerHiddenRowMaterial() {
    SegmentedListItem(
        headlineContent = { Text(stringResource(R.string.settings_home_card_sticker)) },
        supportingContent = {
            Text(stringResource(R.string.settings_home_card_sticker_hidden))
        },
        leadingContent = {
            MaterialIconTile(Icons.Rounded.Star, stringResource(R.string.settings_home_card_sticker))
        },
    )
}

/** Classic/immersive layout connected toggle buttons (mirrors SukiSU's HOME_CARD row). */
@Composable
private fun HomeLayoutButtonGroupMaterial(layout: HomeLayout, actions: ColorPaletteScreenActions) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val layouts = listOf(
            HomeLayout.CLASSIC to R.string.home_layout_classic,
            HomeLayout.IMMERSIVE to R.string.home_layout_immersive,
        )
        layouts.forEachIndexed { index, (candidate, labelRes) ->
            ToggleButton(
                checked = layout == candidate,
                onCheckedChange = {
                    if (it) {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        actions.onSetHomeLayout(candidate)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    layouts.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(stringResource(labelRes))
            }
        }
    }
}

/** Same availability rule as SukiSU: stickers need a banner image plus a background source. */
private fun canUseStickersMaterial(
    visuals: ThemeVisuals,
    cardImagePath: String,
    backgroundPath: String,
): Boolean = visuals.homeLayout == HomeLayout.CLASSIC || (
    visuals.homeCardImageStyle == HomeCardImageStyle.BANNER &&
        cardImagePath.isNotBlank() &&
        (backgroundPath.isNotBlank() || visuals.showHomeCardImageWithoutBackground)
    )

/** Appends the immersive-only rows (image position, alignment, scale sliders). */
private fun addImmersiveOptionsMaterial(
    builder: MutableList<@Composable () -> Unit>,
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
    backgroundPath: String,
) {
    val visuals = state.uiState.themeVisuals
    if (cardImagePath.isNotBlank()) {
        builder.add {
            SegmentedDropdownItem(
                icon = Icons.Rounded.AspectRatio,
                title = stringResource(R.string.settings_home_card_image_position),
                items = listOf(
                    stringResource(R.string.home_card_image_position_banner),
                    stringResource(R.string.home_card_image_position_off),
                ),
                selectedIndex = if (visuals.homeCardImageStyle == HomeCardImageStyle.OFF) 1 else 0,
                onItemSelected = { index ->
                    actions.onSetHomeCardImageStyle(
                        if (index == 1) HomeCardImageStyle.OFF else HomeCardImageStyle.BANNER
                    )
                },
            )
        }
        if (visuals.homeCardImageStyle == HomeCardImageStyle.BANNER) {
            builder.add {
                SegmentedSwitchItem(
                    icon = Icons.Rounded.Wallpaper,
                    title = stringResource(R.string.settings_show_home_card_image_without_background),
                    summary = stringResource(R.string.settings_show_home_card_image_without_background_summary),
                    checked = visuals.showHomeCardImageWithoutBackground,
                    onCheckedChange = actions.onSetShowHomeCardImageWithoutBackground,
                )
            }
        }
    }
    builder.add {
        SegmentedDropdownItem(
            icon = Icons.Rounded.AspectRatio,
            title = stringResource(R.string.settings_home_status_alignment),
            items = listOf(
                stringResource(R.string.home_status_alignment_start),
                stringResource(R.string.home_status_alignment_end),
            ),
            selectedIndex = if (visuals.homeStatusAlignment == HomeStatusAlignment.END) 1 else 0,
            onItemSelected = { index ->
                actions.onSetHomeStatusAlignment(
                    if (index == 1) HomeStatusAlignment.END else HomeStatusAlignment.START
                )
            },
        )
    }
    builder.add {
        MaterialSliderItem(
            icon = Icons.Rounded.Home,
            title = stringResource(R.string.settings_home_pill_scale),
            summaryRes = R.string.settings_home_pill_scale_summary,
            value = visuals.homePillScale,
            valueRange = HomeStatusStyle.PILL_SCALE_MIN..HomeStatusStyle.PILL_SCALE_MAX,
            onValueChangeFinished = actions.onSetHomePillScale,
        )
    }
    if (cardImagePath.isNotBlank() && visuals.homeCardImageStyle == HomeCardImageStyle.BANNER) {
        builder.add {
            MaterialSliderItem(
                icon = Icons.Rounded.AspectRatio,
                title = stringResource(R.string.settings_home_status_scale),
                summaryRes = R.string.settings_home_status_scale_summary,
                value = visuals.homeStatusScale,
                valueRange = HomeStatusStyle.SCALE_MIN..HomeStatusStyle.SCALE_MAX,
                onValueChangeFinished = actions.onSetHomeStatusScale,
            )
        }
        builder.add {
            MaterialSliderItem(
                icon = Icons.Rounded.AspectRatio,
                title = stringResource(R.string.settings_home_status_width),
                summaryRes = R.string.settings_home_status_width_summary,
                value = visuals.homeStatusWidthFraction,
                valueRange = HomeStatusStyle.WIDTH_MIN..HomeStatusStyle.WIDTH_MAX,
                onValueChangeFinished = actions.onSetHomeStatusWidthFraction,
            )
        }
    }
}

/** Inline slider row: icon + title/summary with a live percent, mirroring SukiSU. */
@Composable
private fun MaterialSliderItem(
    icon: ImageVector,
    title: String,
    @StringRes summaryRes: Int,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Float) -> Unit,
) {
    SegmentedBoxItem {
        val sliderState = rememberSliderState(value = value, trackRange = valueRange)
        LaunchedEffect(value) { sliderState.value = value }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(summaryRes, (sliderState.value * 100).roundToInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Slider(
                state = sliderState,
                onValueChangeFinished = { onValueChangeFinished(sliderState.value) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** DETAILS: module description max lines slider (mirrors SukiSU's DETAILS category). */
@Composable
private fun DetailsSectionMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    val content: List<@Composable () -> Unit> = buildList {
        add { MaterialCategoryHeader(ThemeSettingsCategory.DETAILS) }
        add {
            SegmentedBoxItem {
                DetailsLinesSliderMaterial(uiState.moduleDescriptionMaxLines, actions)
            }
        }
    }
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp), content = content)
}

@Composable
private fun DetailsLinesSliderMaterial(
    maxLines: Int,
    actions: ColorPaletteScreenActions,
) {
    val sliderState = rememberSliderState(value = maxLines.toFloat(), trackRange = 1f..5f)
    LaunchedEffect(maxLines) { sliderState.value = maxLines.toFloat() }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Description,
                contentDescription = stringResource(R.string.settings_module_description_max_lines),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_module_description_max_lines),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.settings_module_description_max_lines_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${sliderState.value.roundToInt()} " + stringResource(R.string.unit_lines),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            state = sliderState,
            onValueChangeFinished = {
                actions.onSetModuleDescriptionMaxLines(sliderState.value.roundToInt())
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Sticker entry + expandable editor body (stage, thumbnails, buttons, transform sliders). */
@Composable
private fun StickerEditorMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    val size = state.uiState.themeVisuals.stickerTransforms.size
    var expanded by rememberSaveable { mutableStateOf(false) }
    val summary = if (size == 0) {
        stringResource(R.string.settings_home_card_sticker_not_set)
    } else {
        stringResource(R.string.settings_home_card_sticker_count, size)
    } + " - " + stringResource(R.string.settings_home_card_sticker_summary)
    Column {
        SegmentedListItem(
            onClick = { expanded = !expanded },
            headlineContent = { Text(stringResource(R.string.settings_home_card_sticker)) },
            supportingContent = { Text(summary) },
            leadingContent = {
                MaterialIconTile(Icons.Rounded.Star, stringResource(R.string.settings_home_card_sticker))
            },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        AnimatedVisibility(visible = expanded) {
            StickerEditorBodyMaterial(state, actions, cardImagePath)
        }
    }
}

/** Sticker editor body: stage + thumbnails + add/remove buttons + rotation/size sliders. */
@Composable
private fun StickerEditorBodyMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    val stickers = rememberThemeHomeCardStickers()
    var draft by remember { mutableStateOf(stickers) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(stickers) { draft = stickers }
    LaunchedEffect(draft.size) {
        if (selected > draft.lastIndex) selected = draft.lastIndex.coerceAtLeast(0)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StickerStageBlockMaterial(
            state = state,
            draft = draft,
            selected = selected,
            cardImagePath = cardImagePath,
            onSelect = { selected = it },
            onTransformChange = { index, transform ->
                draft = draft.mapIndexed { i, sticker ->
                    if (i == index) sticker.copy(transform = transform) else sticker
                }
            },
            onTransformCommit = { index, transform ->
                actions.onUpdateHomeCardStickerTransform(index, transform)
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            draft.forEachIndexed { index, sticker ->
                HomeCardStickerThumbnail(
                    path = sticker.path,
                    selected = index == selected,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { selected = index },
                )
            }
        }
        StickerAddRemoveRowMaterial(actions, draft, selected)
        if (draft.isNotEmpty()) {
            StickerTransformSlidersMaterial(state, actions, draft, selected.coerceIn(0, draft.lastIndex)) {
                updated -> draft = updated
            }
        }
    }
}

/** The draggable stage between the editor header and the thumbnail strip. */
@Composable
private fun StickerStageBlockMaterial(
    state: ColorPaletteUiState,
    draft: List<HomeCardSticker>,
    selected: Int,
    cardImagePath: String,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
) {
    val visuals = state.uiState.themeVisuals
    val immersive = visuals.homeLayout == HomeLayout.IMMERSIVE
    HomeCardStickerStage(
        backgroundImagePath = cardImagePath,
        stickers = draft,
        selectedIndex = selected,
        onSelect = onSelect,
        onTransformChange = onTransformChange,
        onTransformCommit = onTransformCommit,
        containerColor = if (immersive) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.primary
        },
        contentColor = if (immersive) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onPrimary
        },
        accentColor = if (immersive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onPrimary
        },
        layout = visuals.homeLayout,
        imageStyle = visuals.homeCardImageStyle,
        statusScale = visuals.homeStatusScale,
        statusWidthFraction = visuals.homeStatusWidthFraction,
        pillScale = visuals.homePillScale,
        statusAlignment = visuals.homeStatusAlignment,
    )
}

/** Add/remove buttons under the stage (mirrors SukiSU's Material editor). */
@Composable
private fun StickerAddRemoveRowMaterial(
    actions: ColorPaletteScreenActions,
    draft: List<HomeCardSticker>,
    selected: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (draft.size < HomeCardSticker.MAX_COUNT) {
            TextButton(onClick = actions.onPickHomeCardSticker) {
                Text(stringResource(R.string.settings_home_card_sticker_add))
            }
        } else {
            Text(
                text = stringResource(R.string.settings_home_card_sticker_limit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (draft.isNotEmpty()) {
            TextButton(onClick = { actions.onRemoveHomeCardSticker(selected) }) {
                Text(stringResource(R.string.settings_home_card_sticker_remove))
            }
        }
    }
}

/** Rotation and size sliders bound to the selected sticker's draft transform. */
@Composable
private fun StickerTransformSlidersMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    draft: List<HomeCardSticker>,
    editIndex: Int,
    onDraftChange: (List<HomeCardSticker>) -> Unit,
) {
    val visuals = state.uiState.themeVisuals
    val editTransform = draft[editIndex].transform
    val sizeMax = if (visuals.homeLayout == HomeLayout.IMMERSIVE) {
        minOf(
            HomeCardStickerTransform.SIZE_MAX,
            0.9f * HomeStatusUi.BannerHeight.value * visuals.homeStatusScale / 360f,
        )
    } else {
        HomeCardStickerTransform.SIZE_MAX
    }
    val rotationValue = editTransform.rotation
    val rotationState = rememberSliderState(
        value = rotationValue,
        trackRange = HomeCardStickerTransform.ROTATION_MIN..HomeCardStickerTransform.ROTATION_MAX,
    )
    LaunchedEffect(rotationValue) { rotationState.value = rotationValue }
    val sizeValue = editTransform.size.coerceIn(HomeCardStickerTransform.SIZE_MIN, sizeMax)
    val sizeState = rememberSliderState(
        value = sizeValue,
        trackRange = HomeCardStickerTransform.SIZE_MIN..sizeMax,
    )
    LaunchedEffect(sizeValue) { sizeState.value = sizeValue }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.settings_home_card_sticker_rotation),
            style = MaterialTheme.typography.titleSmall,
        )
        Slider(
            state = rotationState,
            onValueChange = { value ->
                onDraftChange(
                    draft.mapIndexed { i, sticker ->
                        if (i == editIndex) {
                            sticker.copy(transform = sticker.transform.copy(rotation = value))
                        } else {
                            sticker
                        }
                    }
                )
            },
            onValueChangeFinished = {
                draft.getOrNull(editIndex)?.transform?.let { latest ->
                    val snapped = latest.copy(rotation = latest.rotation.roundToInt().toFloat())
                    actions.onUpdateHomeCardStickerTransform(editIndex, snapped)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.settings_home_card_sticker_size),
            style = MaterialTheme.typography.titleSmall,
        )
        Slider(
            state = sizeState,
            onValueChange = { value ->
                onDraftChange(
                    draft.mapIndexed { i, sticker ->
                        if (i == editIndex) {
                            sticker.copy(transform = sticker.transform.copy(size = value))
                        } else {
                            sticker
                        }
                    }
                )
            },
            onValueChangeFinished = {
                draft.getOrNull(editIndex)?.transform?.let { latest ->
                    actions.onUpdateHomeCardStickerTransform(editIndex, latest)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}



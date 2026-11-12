package aefox.reyang.ui.screen.colorpalette

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Colorize
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
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WaterDrop
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import java.io.File
import kotlin.math.roundToInt
import aefox.reyang.R
import aefox.reyang.ui.component.HomeCardStickerStage
import aefox.reyang.ui.component.HomeCardStickerThumbnail
import aefox.reyang.ui.component.bottombar.BottomBarDestination
import aefox.reyang.ui.component.miuix.ScaleDialog
import aefox.reyang.ui.screen.home.HomeStatusUi
import aefox.reyang.ui.theme.LocalEnableBlur
import aefox.reyang.ui.theme.themeCustomText
import aefox.reyang.ui.theme.ThemeVisuals
import aefox.reyang.ui.theme.isInDarkTheme
import aefox.reyang.ui.theme.keyColorOptions
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import aefox.reyang.ui.theme.rememberThemeHomeCardStickers
import aefox.reyang.ui.theme.themeBackgroundFile
import aefox.reyang.ui.theme.themeHomeCardImageFile
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeCardSticker
import aefox.reyang.ui.theme.themepackage.HomeCardStickerTransform
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle
import aefox.reyang.ui.util.BlurredBar
import aefox.reyang.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun ColorPaletteScreenMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlurState = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlurState)
    val blurActive = backdrop != null
    val barColor = if (blurActive || rememberThemeBackgroundActive()) Color.Transparent else colorScheme.surface
    val uiState = state.uiState
    val currentColorMode = state.currentColorMode
    val isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme()
    val pinnedHeader = LocalConfiguration.current.screenHeightDp >= 600
    val context = LocalContext.current

    // Live drag value: the preview follows the slider before it commits to the repository.
    var pageScaleDraft by remember { mutableFloatStateOf(uiState.pageScale) }
    val visuals = uiState.themeVisuals
    val backgroundPath = themeBackgroundFile(context, visuals.adaptive, isDark)?.absolutePath.orEmpty()
    val previewSpec = remember(
        uiState.keyColor,
        uiState.miuixMonet,
        isDark,
        state.currentPaletteStyle,
        state.currentColorSpec,
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
            miuixMonet = uiState.miuixMonet,
            paletteStyle = state.currentPaletteStyle,
            colorSpec = state.currentColorSpec,
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
    ) + " · " + state.currentPaletteStyle.name
    var selectedCategoryKey by rememberSaveable { mutableStateOf(ThemeSettingsCategory.COLORS.name) }
    // Fullscreen live preview: the compact card opens the two-style pager viewer.
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    val selectedCategory = ThemeSettingsCategory.entries
        .firstOrNull { it.name == selectedCategoryKey }
        ?: ThemeSettingsCategory.COLORS

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = themeCustomText("page.appearance", stringResource(R.string.settings_theme)),
                    navigationIcon = {
                        IconButton(
                            onClick = actions.onBack
                        ) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = null,
                                tint = colorScheme.onBackground
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (pinnedHeader) {
                    MiuixCompactPreviewCard(
                        spec = previewSpec,
                        modeLabel = modeLabel,
                        onClick = { showFullPreview = true },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp),
                    )
                    MiuixCategoryGrid(
                        selected = selectedCategory,
                        onSelect = { selectedCategoryKey = it.name },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .scrollEndHaptic()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 12.dp),
                    overscrollEffect = null,
                ) {
                    if (!pinnedHeader) {
                        item {
                            MiuixCompactPreviewCard(
                                spec = previewSpec,
                                modeLabel = modeLabel,
                                onClick = { showFullPreview = true },
                            )
                            MiuixCategoryGrid(
                                selected = selectedCategory,
                                onSelect = { selectedCategoryKey = it.name },
                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                            )
                        }
                    }
                    if (selectedCategory == ThemeSettingsCategory.COLORS) {
                        item { ColorsCardMiuix(state, actions) }
                    }
                    if (selectedCategory == ThemeSettingsCategory.BACKGROUND) {
                        item { BackgroundCardMiuix(state, actions) }
                    }
                    if (selectedCategory == ThemeSettingsCategory.ICON_TEXT) {
                        item { IconTextCardMiuix(actions) }
                    }
                    if (selectedCategory == ThemeSettingsCategory.HOME_CARD) {
                        item { HomeCardCardMiuix(state, actions) }
                    }
                    if (selectedCategory == ThemeSettingsCategory.BOTTOM_BAR) {
                        item { BottomBarCardMiuix(state, actions) }
                    }
                    if (selectedCategory == ThemeSettingsCategory.INTERACTION) {
                        item {
                            InteractionCardMiuix(
                                state = state,
                                actions = actions,
                                pageScaleDraft = pageScaleDraft,
                                onPageScaleDraft = { pageScaleDraft = it },
                            )
                        }
                    }
                    if (selectedCategory == ThemeSettingsCategory.DETAILS) {
                        item { DetailsCardMiuix(state, actions) }
                    }
                    if (selectedCategory == ThemeSettingsCategory.THEME_PACKAGE) {
                        item { ThemePackageCardMiuix(state, actions) }
                    }
                    item {
                        Spacer(
                            Modifier.height(
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() +
                                        12.dp
                            )
                        )
                    }
                }
            }
        }
        // Fullscreen preview: page 0 renders the Miuix style, page 1 the Material style
        // of the same live spec (mirrors SukiSU).
        ThemeFullscreenPreviewDialog(
            show = showFullPreview,
            onDismiss = { showFullPreview = false },
        ) { previewModifier, page ->
            if (page == 0) {
                ThemePreviewMiuix(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            } else {
                ThemePreviewMaterial(
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
private fun MiuixCompactPreviewCard(
    spec: ThemePreviewSpec,
    modeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        showIndication = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemePreviewMiuix(
                spec = spec,
                modifier = Modifier.width(88.dp),
                compact = true,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_preview_caption_miuix),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = modeLabel,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                ThemePaletteSwatches(
                    colors = listOf(
                        colorScheme.primary,
                        colorScheme.secondaryContainer,
                        colorScheme.tertiaryContainer,
                        colorScheme.surfaceContainerHighest,
                    ),
                )
                Text(
                    text = stringResource(R.string.theme_preview_compact_summary),
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

/** The category box: only one category's settings are composed at a time. */
@Composable
private fun MiuixCategoryGrid(
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeSettingsCategory.entries.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { category ->
                        MiuixCategoryTile(
                            category = category,
                            selected = category == selected,
                            onClick = { onSelect(category) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCategoryTile(
    category: ThemeSettingsCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.surfaceContainerHighest,
        label = "ThemeCategoryBackground",
    )
    val contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurface
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .selectable(
                selected = selected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
                role = Role.Tab,
            )
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(category.labelRes),
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MiuixCategoryHeader(
    category: ThemeSettingsCategory,
    modifier: Modifier = Modifier,
    summary: String? = null,
) {
    val title = stringResource(category.titleRes)
    BasicComponent(
        title = title,
        summary = summary ?: stringResource(category.summaryRes),
        modifier = modifier,
        startAction = {
            Icon(
                imageVector = category.icon,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = title,
                tint = colorScheme.onBackground,
            )
        },
    )
}

/** COLORS: theme mode tabs, Monet, key color, palette style/spec and the appearance overlay. */
@Composable
private fun ColorsCardMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.COLORS)
        HorizontalDivider()
        val themeItems = listOf(
            stringResource(id = R.string.settings_theme_mode_system),
            stringResource(id = R.string.settings_theme_mode_light),
            stringResource(id = R.string.settings_theme_mode_dark),
        )
        // TabRowWithContour keeps every tab inside the row width on narrow screens
        // (the plain TabRow overflows and clips the last tab; mirrors SukiSU).
        TabRowWithContour(
            tabs = themeItems,
            selectedTabIndex = (if (uiState.themeMode >= 3) uiState.themeMode - 3 else uiState.themeMode).coerceIn(0, 2),
            onTabSelected = { index ->
                actions.onSetThemeMode(index)
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            colors = TabRowDefaults.tabRowColors(
                backgroundColor = colorScheme.surfaceContainerHighest,
                selectedBackgroundColor = colorScheme.surfaceContainer,
            ),
        )
        HorizontalDivider()
        SwitchPreference(
            title = stringResource(id = R.string.settings_monet),
            startAction = {
                MiuixRowIcon(Icons.Rounded.Wallpaper, stringResource(id = R.string.settings_monet))
            },
            checked = uiState.miuixMonet,
            onCheckedChange = {
                actions.onSetMiuixMonet(it)
            }
        )
        AnimatedVisibility(visible = uiState.miuixMonet) {
            KeyColorSectionMiuix(state, actions)
        }
        HorizontalDivider()
        ArrowPreference(
            title = stringResource(R.string.appearance_overlay_title),
            summary = stringResource(
                if (uiState.themeAppearanceJson.isNotBlank()) {
                    R.string.appearance_overlay_active
                } else {
                    R.string.appearance_overlay_summary
                }
            ),
            startAction = {
                MiuixRowIcon(Icons.Rounded.Palette, stringResource(R.string.appearance_overlay_title))
            },
            onClick = actions.onOpenAppearanceOverlay,
        )
    }
}

/** Key color dropdown + palette style/spec (nested dropdowns, mirrors SukiSU). */
@Composable
private fun KeyColorSectionMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    Column {
        val colorItems = listOf(
            stringResource(id = R.string.settings_key_color_default),
            stringResource(id = R.string.color_red),
            stringResource(id = R.string.color_pink),
            stringResource(id = R.string.color_purple),
            stringResource(id = R.string.color_deep_purple),
            stringResource(id = R.string.color_indigo),
            stringResource(id = R.string.color_blue),
            stringResource(id = R.string.color_cyan),
            stringResource(id = R.string.color_teal),
            stringResource(id = R.string.color_green),
            stringResource(id = R.string.color_yellow),
            stringResource(id = R.string.color_amber),
            stringResource(id = R.string.color_orange),
            stringResource(id = R.string.color_brown),
            stringResource(id = R.string.color_blue_grey),
            stringResource(id = R.string.color_sakura),
        )
        val colorValues = listOf(0) + keyColorOptions
        MiuixDropdownRow(
            title = stringResource(id = R.string.settings_key_color),
            icon = Icons.Rounded.Colorize,
            items = colorItems,
            // A background-derived seed is not one of the fixed options.
            selectedIndex = colorValues.indexOf(
                if (uiState.extractColorFromBackground) 0 else uiState.keyColor
            ).takeIf { it >= 0 } ?: 0,
            onSelectedIndexChange = { index ->
                actions.onSetKeyColor(colorValues[index])
            },
        )
        AnimatedVisibility(visible = uiState.keyColor != 0) {
            StyleSpecSectionMiuix(state, actions)
        }
    }
}

/** Palette style + color spec dropdowns, shown once a key color is picked. */
@Composable
private fun StyleSpecSectionMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    Column {
        val styles = PaletteStyle.entries
        MiuixDropdownRow(
            title = stringResource(R.string.settings_color_style),
            icon = Icons.Rounded.Style,
            items = styles.map { it.name },
            selectedIndex = styles.indexOfFirst { it.name == uiState.colorStyle }.coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                actions.onSetColorStyle(styles[index].name)
            },
        )
        val specs = ColorSpec.SpecVersion.entries
        MiuixDropdownRow(
            title = stringResource(R.string.settings_color_spec),
            icon = Icons.Rounded.DesignServices,
            items = specs.map { it.name },
            selectedIndex = specs.indexOfFirst { it.name == uiState.colorSpec }.coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                actions.onSetColorSpec(specs[index].name)
            },
        )
    }
}

/** BACKGROUND: custom image, re-crop, system wallpaper, color extraction and the appearance sliders. */
@Composable
private fun BackgroundCardMiuix(
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
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.BACKGROUND)
        HorizontalDivider()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MiuixToggleRow(
                title = stringResource(R.string.settings_progressive_blur),
                summary = stringResource(R.string.settings_progressive_blur_summary),
                icon = Icons.Rounded.BlurOn,
                checked = uiState.enableProgressiveBlur,
                onCheckedChange = actions.onSetEnableProgressiveBlur,
            )
            HorizontalDivider()
        }
        if (!uiState.useSystemWallpaper) {
            BasicComponent(
                title = stringResource(R.string.settings_custom_background),
                summary = stringResource(R.string.settings_custom_background_summary),
                startAction = {
                    MiuixRowIcon(Icons.Rounded.Image, stringResource(R.string.settings_custom_background))
                },
                endActions = {
                    if (backgroundPath.isNotBlank()) {
                        TextButton(
                            text = stringResource(R.string.settings_clear),
                            onClick = actions.onClearBackgroundImage,
                        )
                    }
                },
                onClick = actions.onPickBackgroundImage,
            )
            if (backgroundPath.isNotBlank()) {
                HorizontalDivider()
                BasicComponent(
                    title = stringResource(R.string.settings_crop_background),
                    summary = stringResource(R.string.settings_crop_background_summary),
                    startAction = {
                        MiuixRowIcon(Icons.Rounded.Crop, stringResource(R.string.settings_crop_background))
                    },
                    onClick = actions.onCropCurrentBackground,
                )
            }
        }
        HorizontalDivider()
        MiuixToggleRow(
            title = stringResource(R.string.background_system_wallpaper),
            summary = stringResource(R.string.background_system_wallpaper_summary),
            icon = Icons.Rounded.Wallpaper,
            checked = uiState.useSystemWallpaper,
            onCheckedChange = actions.onSetUseSystemWallpaper,
        )
        if (showBackgroundAppearance) {
            HorizontalDivider()
            MiuixToggleRow(
                title = stringResource(R.string.settings_extract_background_color),
                summary = stringResource(
                    if (uiState.miuixMonet) {
                        R.string.settings_extract_background_color_summary
                    } else {
                        R.string.settings_extract_background_color_disabled
                    },
                ),
                icon = Icons.Rounded.Colorize,
                checked = uiState.extractColorFromBackground,
                onCheckedChange = actions.onSetExtractColorFromBackground,
            )
            HorizontalDivider()
            BasicComponent(
                title = stringResource(R.string.settings_background_appearance),
                summary = stringResource(R.string.settings_background_appearance_summary),
                startAction = {
                    MiuixRowIcon(Icons.Rounded.Colorize, stringResource(R.string.settings_background_appearance))
                },
                endActions = {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = if (backgroundAppearanceExpanded) 180f else 0f
                        },
                        tint = colorScheme.onBackground,
                    )
                },
                onClick = {
                    backgroundAppearanceExpanded = !backgroundAppearanceExpanded
                },
            )
            if (backgroundAppearanceExpanded) {
                HorizontalDivider()
                BackgroundSlidersMiuix(visuals, actions)
            }
        }
    }
}

/** Card opacity, blur and scrim sliders: draft during drag, committed on release. */
@Composable
private fun BackgroundSlidersMiuix(
    visuals: ThemeVisuals,
    actions: ColorPaletteScreenActions,
) {
    val cardAlphaDraft = remember(visuals.cardBackgroundAlphaPercent) {
        mutableFloatStateOf(visuals.cardBackgroundAlphaPercent.toFloat())
    }
    var blurDraft by remember(visuals.backgroundBlurPercent) {
        mutableFloatStateOf(visuals.backgroundBlurPercent.toFloat())
    }
    var scrimDraft by remember(visuals.backgroundScrimPercent) {
        mutableFloatStateOf(visuals.backgroundScrimPercent.toFloat())
    }
    MiuixSliderSection(
        title = stringResource(R.string.settings_card_background_alpha),
        summary = stringResource(R.string.settings_card_background_alpha_summary),
        value = cardAlphaDraft.floatValue,
        valueRange = 10f..91f,
        onValueChange = { cardAlphaDraft.floatValue = it },
        onValueChangeFinished = { actions.onSetCardBackgroundAlphaPercent(cardAlphaDraft.floatValue.roundToInt()) },
    )
    MiuixSliderSection(
        title = stringResource(R.string.settings_background_blur),
        summary = stringResource(R.string.settings_background_blur_summary, blurDraft.roundToInt()),
        value = blurDraft,
        valueRange = 0f..100f,
        onValueChange = { blurDraft = it },
        onValueChangeFinished = { actions.onSetCustomBackgroundBlurPercent(blurDraft.roundToInt()) },
    )
    MiuixSliderSection(
        title = stringResource(R.string.settings_background_scrim),
        summary = stringResource(R.string.settings_background_scrim_summary, scrimDraft.roundToInt()),
        value = scrimDraft,
        valueRange = -100f..100f,
        onValueChange = { scrimDraft = it },
        onValueChangeFinished = { actions.onSetCustomBackgroundScrimPercent(scrimDraft.roundToInt()) },
        startLabel = stringResource(R.string.settings_background_scrim_darker),
        endLabel = stringResource(R.string.settings_background_scrim_lighter),
    )
}

/** BOTTOM_BAR: the four tab icon slot rows, blur, floating bar and its liquid glass effect. */
@Composable
private fun BottomBarCardMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.BOTTOM_BAR)
        HorizontalDivider()
        BottomBarIconSlotRowsMiuix(uiState.themeVisuals.bottomBarIcons, actions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MiuixToggleRow(
                title = stringResource(id = R.string.settings_enable_blur),
                summary = stringResource(id = R.string.settings_enable_blur_summary),
                icon = Icons.Rounded.BlurOn,
                checked = uiState.enableBlur,
                onCheckedChange = actions.onSetEnableBlur,
            )
        }
        MiuixToggleRow(
            title = stringResource(id = R.string.settings_floating_bottom_bar),
            summary = stringResource(id = R.string.settings_floating_bottom_bar_summary),
            icon = Icons.Rounded.CallToAction,
            checked = uiState.enableFloatingBottomBar,
            onCheckedChange = actions.onSetEnableFloatingBottomBar,
        )
        AnimatedVisibility(visible = uiState.enableFloatingBottomBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MiuixToggleRow(
                title = stringResource(id = R.string.settings_enable_glass),
                summary = stringResource(id = R.string.settings_enable_glass_summary),
                icon = Icons.Rounded.WaterDrop,
                checked = uiState.enableFloatingBottomBarBlur,
                onCheckedChange = actions.onSetEnableFloatingBottomBarBlur,
            )
        }
        MiuixToggleRow(
            title = stringResource(id = R.string.settings_navigation_badge),
            summary = stringResource(id = R.string.settings_navigation_badge_summary),
            icon = Icons.Rounded.Pin,
            checked = uiState.enableNavigationBadge,
            onCheckedChange = actions.onSetEnableNavigationBadge,
        )
    }
}

/** The four tab icon slot rows: preview + pick + clear (mirrors SukiSU's slot row form). */
@Composable
private fun BottomBarIconSlotRowsMiuix(
    iconPaths: Map<String, String>,
    actions: ColorPaletteScreenActions,
) {
    BottomBarDestination.entries.forEach { destination ->
        val path = iconPaths[destination.slotKey]?.takeIf { it.isNotBlank() }
        val label = stringResource(destination.label)
        ArrowPreference(
            title = label,
            summary = stringResource(
                if (path != null) {
                    R.string.settings_bottom_bar_icon_custom_summary
                } else {
                    R.string.settings_bottom_bar_icon_default_summary
                }
            ),
            startAction = { BottomBarSlotPreviewMiuix(path, destination.icon, label) },
            endActions = {
                if (path != null) {
                    TextButton(
                        text = stringResource(R.string.settings_clear),
                        onClick = { actions.onClearBottomBarIcon(destination.slotKey) },
                    )
                }
            },
            onClick = { actions.onPickBottomBarIcon(destination.slotKey) },
        )
    }
}

/** Slot leading preview: the custom image when set, otherwise the built-in tab icon. */
@Composable
private fun BottomBarSlotPreviewMiuix(path: String?, icon: ImageVector, contentDescription: String) {
    if (path != null) {
        AsyncImage(
            model = File(path),
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(end = 6.dp)
                .size(24.dp),
        )
    } else {
        MiuixRowIcon(icon, contentDescription)
    }
}

/** INTERACTION: predictive back, swipe dismiss, pager gestures and page scale. */
@Composable
private fun InteractionCardMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    pageScaleDraft: Float,
    onPageScaleDraft: (Float) -> Unit,
) {
    val uiState = state.uiState
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.INTERACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            MiuixToggleRow(
                title = stringResource(id = R.string.settings_enable_predictive_back),
                summary = stringResource(id = R.string.settings_enable_predictive_back_summary),
                icon = Icons.AutoMirrored.Rounded.MenuOpen,
                checked = uiState.enablePredictiveBack,
                onCheckedChange = actions.onSetEnablePredictiveBack,
            )
        }
        MiuixToggleRow(
            title = stringResource(R.string.settings_enable_swipe_dismiss),
            summary = stringResource(R.string.settings_enable_swipe_dismiss_summary),
            icon = Icons.Rounded.Swipe,
            checked = uiState.enableSwipeDismiss,
            onCheckedChange = actions.onSetEnableSwipeDismiss,
        )
        MiuixDropdownRow(
            title = stringResource(R.string.settings_pager_gesture_mode),
            icon = Icons.Rounded.ViewCarousel,
            items = listOf(
                stringResource(R.string.settings_pager_gesture_native),
                stringResource(R.string.settings_pager_gesture_cross_axis),
                stringResource(R.string.settings_pager_gesture_ios_like),
            ),
            selectedIndex = uiState.pagerInterceptionMode,
            onSelectedIndexChange = actions.onSetPagerInterceptionMode,
        )
        PageScalePreferenceMiuix(pageScaleDraft, onPageScaleDraft, actions)
    }
}

/** Page scale preference: inline slider (preview follows the live draft) + scale dialog. */
@Composable
private fun PageScalePreferenceMiuix(
    pageScaleDraft: Float,
    onPageScaleDraft: (Float) -> Unit,
    actions: ColorPaletteScreenActions,
) {
    val showScaleDialog = rememberSaveable { mutableStateOf(false) }
    ArrowPreference(
        title = stringResource(id = R.string.settings_page_scale),
        summary = stringResource(id = R.string.settings_page_scale_summary),
        startAction = {
            MiuixRowIcon(Icons.Rounded.AspectRatio, stringResource(id = R.string.settings_page_scale))
        },
        endActions = {
            Text(
                text = "${(pageScaleDraft * 100).toInt()}%",
                color = colorScheme.onSurfaceVariantActions,
            )
        },
        onClick = { showScaleDialog.value = !showScaleDialog.value },
        holdDownState = showScaleDialog.value,
        bottomAction = {
            Slider(
                value = pageScaleDraft,
                onValueChange = onPageScaleDraft,
                onValueChangeFinished = {
                    actions.onSetPageScale(pageScaleDraft)
                },
                valueRange = 0.8f..1.1f,
                showKeyPoints = true,
                keyPoints = listOf(0.8f, 0.9f, 1f, 1.1f),
                magnetThreshold = 0.01f,
                hapticEffect = SliderDefaults.SliderHapticEffect.Step,
            )
        },
    )
    ScaleDialog(
        show = showScaleDialog.value,
        onDismissRequest = { showScaleDialog.value = false },
        volumeState = { pageScaleDraft },
        onVolumeChange = {
            onPageScaleDraft(it)
            actions.onSetPageScale(it)
        }
    )
}

/**
 * THEME_PACKAGE: import / export / reset / clear-all in the primary card, with the
 * active theme, trial banner, adaptive builder, app icon and restore-default in a
 * trailing card (mirrors SukiSU's block layout).
 */
@Composable
private fun ThemePackageCardMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val showResetDialog = rememberSaveable { mutableStateOf(false) }
    val showClearAllDialog = rememberSaveable { mutableStateOf(false) }
    val uiState = state.uiState
    val activeTheme = if (uiState.themeActiveName.isBlank()) {
        stringResource(R.string.theme_pkg_no_theme)
    } else {
        uiState.themeActiveName
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(
            category = ThemeSettingsCategory.THEME_PACKAGE,
            summary = stringResource(R.string.theme_pkg_current_theme) + ": " + activeTheme,
        )
        if (uiState.themeTrialActive) {
            ArrowPreference(
                title = stringResource(R.string.theme_pkg_trial_banner, uiState.themeActiveName),
                endActions = {
                    TextButton(
                        text = stringResource(R.string.theme_pkg_cancel_trial),
                        onClick = actions.onCancelTrial,
                    )
                },
                onClick = {},
            )
        }
        ThemePackageRowMiuix(
            title = stringResource(R.string.theme_pkg_import_local),
            icon = Icons.Rounded.FileDownload,
            onClick = actions.onImportTheme,
        )
        ThemePackageRowMiuix(
            title = stringResource(R.string.theme_pkg_export_current),
            icon = Icons.Rounded.FileUpload,
            onClick = actions.onExportTheme,
        )
        ThemePackageRowMiuix(
            title = stringResource(R.string.theme_reset),
            summary = stringResource(R.string.theme_reset_summary),
            icon = Icons.Rounded.Restore,
            onClick = { showResetDialog.value = true },
        )
        ThemePackageRowMiuix(
            title = stringResource(R.string.theme_clear_all),
            summary = stringResource(R.string.theme_clear_all_summary),
            icon = Icons.Rounded.Delete,
            onClick = { showClearAllDialog.value = true },
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        ThemePackageRowMiuix(
            title = stringResource(R.string.theme_pkg_restore_default),
            summary = stringResource(R.string.theme_pkg_restore_default_body),
            icon = Icons.Rounded.Restore,
            onClick = actions.onRestoreDefault,
        )
    }
    ThemeConfirmDialogMiuix(
        show = showResetDialog.value,
        title = stringResource(R.string.theme_reset_confirm_title),
        summary = stringResource(R.string.theme_reset_confirm_message),
        confirmText = stringResource(R.string.theme_reset),
        onDismiss = { showResetDialog.value = false },
        onConfirm = {
            showResetDialog.value = false
            actions.onResetBackgroundAppearance()
        },
    )
    ThemeConfirmDialogMiuix(
        show = showClearAllDialog.value,
        title = stringResource(R.string.theme_clear_all_confirm_title),
        summary = stringResource(R.string.theme_clear_all_confirm_message),
        confirmText = stringResource(R.string.theme_clear_all),
        onDismiss = { showClearAllDialog.value = false },
        onConfirm = {
            showClearAllDialog.value = false
            actions.onClearAllAppearance()
        },
    )
}

/** A theme package action row: leading icon + title (+ optional summary). */
@Composable
private fun ThemePackageRowMiuix(
    title: String,
    icon: ImageVector,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
) {
    BasicComponent(
        title = title,
        summary = summary,
        onClick = onClick,
        startAction = {
            MiuixRowIcon(icon, title)
        },
    )
}

/** Leading row icon tinted with the page content color. */
@Composable
private fun MiuixRowIcon(icon: ImageVector, contentDescription: String) {
    Icon(
        icon,
        modifier = Modifier.padding(end = 6.dp),
        contentDescription = contentDescription,
        tint = colorScheme.onBackground,
    )
}

/** Switch preference row with a leading icon. */
@Composable
private fun MiuixToggleRow(
    title: String,
    summary: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = title,
        summary = summary,
        startAction = { MiuixRowIcon(icon, title) },
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

/** Dropdown preference row with a leading icon. */
@Composable
private fun MiuixDropdownRow(
    title: String,
    icon: ImageVector,
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
) {
    OverlayDropdownPreference(
        title = title,
        items = items,
        startAction = { MiuixRowIcon(icon, title) },
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
    )
}

/** Shared confirm dialog for the appearance-wide reset/clear operations. */
@Composable
private fun ThemeConfirmDialogMiuix(
    show: Boolean,
    title: String,
    summary: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!show) return
    OverlayDialog(
        show = true,
        title = title,
        summary = summary,
        onDismissRequest = onDismiss,
        content = {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    text = stringResource(android.R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        },
    )
}

/** ICON_TEXT: launcher icon picker and the custom interface texts entry. */
@Composable
private fun IconTextCardMiuix(
    actions: ColorPaletteScreenActions,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.ICON_TEXT)
        HorizontalDivider()
        ArrowPreference(
            title = stringResource(R.string.app_icon_title),
            summary = stringResource(R.string.app_icon_summary),
            startAction = {
                MiuixRowIcon(Icons.Rounded.AppShortcut, stringResource(R.string.app_icon_title))
            },
            onClick = actions.onOpenAppIcon,
        )
        HorizontalDivider()
        ArrowPreference(
            title = stringResource(R.string.settings_custom_texts),
            summary = stringResource(R.string.settings_custom_texts_summary),
            startAction = {
                MiuixRowIcon(Icons.Rounded.Edit, stringResource(R.string.settings_custom_texts))
            },
            onClick = actions.onOpenCustomTexts,
        )
    }
}

/** A labeled text field: label above, input below (avoids the floating-label overlap). */
@Composable
internal fun MiuixTextFieldRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = "",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            useLabelAsPlaceholder = false,
        )
    }
}

/** HOME_CARD: layout, card image, immersive display options, sliders and sticker editor. */
@Composable
private fun HomeCardCardMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val visuals = state.uiState.themeVisuals
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val cardImagePath = themeHomeCardImageFile(context, visuals.adaptive, dark)?.absolutePath.orEmpty()
    val backgroundPath = themeBackgroundFile(context, visuals.adaptive, dark)?.absolutePath.orEmpty()
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.HOME_CARD)
        HorizontalDivider()
        HomeLayoutTabsMiuix(visuals.homeLayout, actions)
        ArrowPreference(
            title = stringResource(R.string.settings_home_card_image),
            summary = stringResource(
                if (cardImagePath.isBlank()) {
                    R.string.settings_home_card_image_default
                } else {
                    R.string.settings_home_card_image_set
                }
            ),
            startAction = {
                MiuixRowIcon(Icons.Rounded.Image, stringResource(R.string.settings_home_card_image))
            },
            onClick = actions.onPickHomeCardImage,
        )
        HomeCardClassicOptionsMiuix(visuals, actions, cardImagePath)
        HomeCardImmersiveOptionsMiuix(visuals, actions, cardImagePath)
        HomeCardStickerEntryMiuix(state, actions, cardImagePath, backgroundPath)
        if (cardImagePath.isNotBlank()) {
            ArrowPreference(
                title = stringResource(R.string.settings_home_card_image_clear),
                summary = stringResource(R.string.settings_home_card_image_clear_summary),
                startAction = {
                    MiuixRowIcon(Icons.Rounded.Restore, stringResource(R.string.settings_home_card_image_clear))
                },
                onClick = actions.onClearHomeCardImage,
            )
        }
    }
}

/** Classic/immersive layout tabs (mirrors SukiSU's TabRowWithContour row). */
@Composable
private fun HomeLayoutTabsMiuix(layout: HomeLayout, actions: ColorPaletteScreenActions) {
    TabRowWithContour(
        tabs = listOf(
            stringResource(R.string.home_layout_classic),
            stringResource(R.string.home_layout_immersive),
        ),
        selectedTabIndex = if (layout == HomeLayout.IMMERSIVE) 1 else 0,
        onTabSelected = { index ->
            actions.onSetHomeLayout(if (index == 1) HomeLayout.IMMERSIVE else HomeLayout.CLASSIC)
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        colors = TabRowDefaults.tabRowColors(
            backgroundColor = colorScheme.surfaceContainerHighest,
            selectedBackgroundColor = colorScheme.surfaceContainer,
        ),
    )
}

/** Options only visible for the classic layout with a card image. */
@Composable
private fun HomeCardClassicOptionsMiuix(
    visuals: ThemeVisuals,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    if (visuals.homeLayout != HomeLayout.CLASSIC || cardImagePath.isBlank()) return
    MiuixToggleRow(
        title = stringResource(R.string.settings_hide_home_card_decoration),
        summary = stringResource(R.string.settings_hide_home_card_decoration_summary),
        icon = Icons.Rounded.Star,
        checked = visuals.hideHomeCardDecoration,
        onCheckedChange = actions.onSetHideHomeCardDecoration,
    )
    MiuixToggleRow(
        title = stringResource(R.string.settings_hide_home_card_status_text),
        summary = stringResource(R.string.settings_hide_home_card_status_text_summary),
        icon = Icons.Rounded.Home,
        checked = visuals.hideHomeCardStatusText,
        onCheckedChange = actions.onSetHideHomeCardStatusText,
    )
}

/** Immersive layout options: image position, alignment and the three scale sliders. */
@Composable
private fun HomeCardImmersiveOptionsMiuix(
    visuals: ThemeVisuals,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
) {
    if (visuals.homeLayout != HomeLayout.IMMERSIVE) return
    val showImageOptions = cardImagePath.isNotBlank()
    if (showImageOptions) {
        MiuixDropdownRow(
            title = stringResource(R.string.settings_home_card_image_position),
            icon = Icons.Rounded.AspectRatio,
            items = listOf(
                stringResource(R.string.home_card_image_position_banner),
                stringResource(R.string.home_card_image_position_off),
            ),
            selectedIndex = if (visuals.homeCardImageStyle == HomeCardImageStyle.OFF) 1 else 0,
            onSelectedIndexChange = { index ->
                actions.onSetHomeCardImageStyle(
                    if (index == 1) HomeCardImageStyle.OFF else HomeCardImageStyle.BANNER
                )
            },
        )
        if (visuals.homeCardImageStyle == HomeCardImageStyle.BANNER) {
            MiuixToggleRow(
                title = stringResource(R.string.settings_show_home_card_image_without_background),
                summary = stringResource(R.string.settings_show_home_card_image_without_background_summary),
                icon = Icons.Rounded.Wallpaper,
                checked = visuals.showHomeCardImageWithoutBackground,
                onCheckedChange = actions.onSetShowHomeCardImageWithoutBackground,
            )
        }
    }
    MiuixDropdownRow(
        title = stringResource(R.string.settings_home_status_alignment),
        icon = Icons.Rounded.AspectRatio,
        items = listOf(
            stringResource(R.string.home_status_alignment_start),
            stringResource(R.string.home_status_alignment_end),
        ),
        selectedIndex = if (visuals.homeStatusAlignment == HomeStatusAlignment.END) 1 else 0,
        onSelectedIndexChange = { index ->
            actions.onSetHomeStatusAlignment(
                if (index == 1) HomeStatusAlignment.END else HomeStatusAlignment.START
            )
        },
    )
    ImmersiveSlidersMiuix(visuals, actions, showImageOptions)
}

/** Pill scale always visible; status scale/width only with a banner image. */
@Composable
private fun ImmersiveSlidersMiuix(
    visuals: ThemeVisuals,
    actions: ColorPaletteScreenActions,
    showImageOptions: Boolean,
) {
    var pillDraft by remember(visuals.homePillScale) { mutableFloatStateOf(visuals.homePillScale) }
    var scaleDraft by remember(visuals.homeStatusScale) { mutableFloatStateOf(visuals.homeStatusScale) }
    var widthDraft by remember(visuals.homeStatusWidthFraction) {
        mutableFloatStateOf(visuals.homeStatusWidthFraction)
    }
    MiuixSliderSection(
        title = stringResource(R.string.settings_home_pill_scale),
        summary = stringResource(R.string.settings_home_pill_scale_summary, (pillDraft * 100).roundToInt()),
        value = pillDraft,
        valueRange = HomeStatusStyle.PILL_SCALE_MIN..HomeStatusStyle.PILL_SCALE_MAX,
        onValueChange = { pillDraft = it },
        onValueChangeFinished = { actions.onSetHomePillScale(pillDraft) },
        startLabel = "50%",
        endLabel = "150%",
    )
    if (showImageOptions && visuals.homeCardImageStyle == HomeCardImageStyle.BANNER) {
        MiuixSliderSection(
            title = stringResource(R.string.settings_home_status_scale),
            summary = stringResource(R.string.settings_home_status_scale_summary, (scaleDraft * 100).roundToInt()),
            value = scaleDraft,
            valueRange = HomeStatusStyle.SCALE_MIN..HomeStatusStyle.SCALE_MAX,
            onValueChange = { scaleDraft = it },
            onValueChangeFinished = { actions.onSetHomeStatusScale(scaleDraft) },
            startLabel = "50%",
            endLabel = "300%",
        )
        MiuixSliderSection(
            title = stringResource(R.string.settings_home_status_width),
            summary = stringResource(R.string.settings_home_status_width_summary, (widthDraft * 100).roundToInt()),
            value = widthDraft,
            valueRange = HomeStatusStyle.WIDTH_MIN..HomeStatusStyle.WIDTH_MAX,
            onValueChange = { widthDraft = it },
            onValueChangeFinished = { actions.onSetHomeStatusWidthFraction(widthDraft) },
            startLabel = "45%",
            endLabel = "100%",
        )
    }
}

/** Inline slider row with title/summary and optional range end labels (mirrors SukiSU). */
@Composable
private fun MiuixSliderSection(
    title: String,
    summary: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    startLabel: String? = null,
    endLabel: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
        Text(text = summary, fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
        Spacer(Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
        )
        if (startLabel != null && endLabel != null) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = startLabel, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.weight(1f))
                Text(text = endLabel, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
            }
        }
    }
}

/** Sticker entry + expandable editor (stage, thumbnails, add/remove, rotation/size). */
@Composable
private fun HomeCardStickerEntryMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    cardImagePath: String,
    backgroundPath: String,
) {
    val visuals = state.uiState.themeVisuals
    val canUseStickers = visuals.homeLayout == HomeLayout.CLASSIC || (
        visuals.homeCardImageStyle == HomeCardImageStyle.BANNER &&
            cardImagePath.isNotBlank() &&
            (backgroundPath.isNotBlank() || visuals.showHomeCardImageWithoutBackground)
        )
    if (!canUseStickers) {
        if (visuals.homeLayout == HomeLayout.IMMERSIVE &&
            visuals.homeCardImageStyle == HomeCardImageStyle.BANNER
        ) {
            ThemePackageRowMiuix(
                title = stringResource(R.string.settings_home_card_sticker),
                summary = stringResource(R.string.settings_home_card_sticker_hidden),
                icon = Icons.Rounded.Star,
            )
        }
        return
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    BasicComponent(
        title = stringResource(R.string.settings_home_card_sticker),
        summary = stickerSummaryText(state) + " - " +
            stringResource(R.string.settings_home_card_sticker_summary),
        startAction = {
            MiuixRowIcon(Icons.Rounded.Star, stringResource(R.string.settings_home_card_sticker))
        },
        endActions = {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = if (expanded) 180f else 0f },
                tint = colorScheme.onBackground,
            )
        },
        onClick = { expanded = !expanded },
    )
    AnimatedVisibility(visible = expanded) {
        StickerEditorBodyMiuix(state, actions, cardImagePath)
    }
}

@Composable
private fun stickerSummaryText(state: ColorPaletteUiState): String {
    val size = state.uiState.themeVisuals.stickerTransforms.size
    return if (size == 0) {
        stringResource(R.string.settings_home_card_sticker_not_set)
    } else {
        stringResource(R.string.settings_home_card_sticker_count, size)
    }
}

/** Sticker editor body: stage + thumbnails + add/remove buttons + rotation/size sliders. */
@Composable
private fun StickerEditorBodyMiuix(
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
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        StickerStageBlockMiuix(
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
        StickerThumbnailRowMiuix(draft, selected, colorScheme.primary) { selected = it }
        StickerAddRemoveRowMiuix(state, actions, draft, selected)
        if (draft.isNotEmpty()) {
            StickerTransformSlidersMiuix(state, actions, draft, selected.coerceIn(0, draft.lastIndex)) {
                updated -> draft = updated
            }
        }
    }
}

/** The draggable stage between the editor header and the thumbnail strip. */
@Composable
private fun StickerStageBlockMiuix(
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
        containerColor = if (immersive) colorScheme.surfaceContainer else colorScheme.secondaryContainer,
        contentColor = if (immersive) colorScheme.onSurface else colorScheme.onSecondaryContainer,
        accentColor = colorScheme.primary,
        layout = visuals.homeLayout,
        imageStyle = visuals.homeCardImageStyle,
        statusScale = visuals.homeStatusScale,
        statusWidthFraction = visuals.homeStatusWidthFraction,
        pillScale = visuals.homePillScale,
        statusAlignment = visuals.homeStatusAlignment,
    )
}

/** Thumbnail strip: one 44dp image per sticker, border highlights the selection. */
@Composable
private fun StickerThumbnailRowMiuix(
    draft: List<HomeCardSticker>,
    selected: Int,
    accentColor: Color,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        draft.forEachIndexed { index, sticker ->
            HomeCardStickerThumbnail(
                path = sticker.path,
                selected = index == selected,
                accentColor = accentColor,
                onClick = { onSelect(index) },
            )
        }
    }
}

/** Add/remove pill buttons under the stage (mirrors SukiSU's TextButton pair). */
@Composable
private fun StickerAddRemoveRowMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
    draft: List<HomeCardSticker>,
    selected: Int,
) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (draft.size < HomeCardSticker.MAX_COUNT) {
            TextButton(
                text = stringResource(R.string.settings_home_card_sticker_add),
                onClick = actions.onPickHomeCardSticker,
                minWidth = 0.dp,
                minHeight = 0.dp,
                cornerRadius = 100.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        } else {
            Text(
                text = stringResource(R.string.settings_home_card_sticker_limit),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariantSummary,
            )
        }
        if (draft.isNotEmpty()) {
            TextButton(
                text = stringResource(R.string.settings_home_card_sticker_remove),
                onClick = { actions.onRemoveHomeCardSticker(selected) },
                minWidth = 0.dp,
                minHeight = 0.dp,
                cornerRadius = 100.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

/** Rotation and size sliders bound to the selected sticker's draft transform. */
@Composable
private fun StickerTransformSlidersMiuix(
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
    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    MiuixSliderSection(
        title = stringResource(R.string.settings_home_card_sticker_rotation),
        summary = "${editTransform.rotation.roundToInt()}°",
        value = editTransform.rotation,
        valueRange = HomeCardStickerTransform.ROTATION_MIN..HomeCardStickerTransform.ROTATION_MAX,
        onValueChange = { value ->
            onDraftChange(
                draft.mapIndexed { i, sticker ->
                    if (i == editIndex) sticker.copy(transform = sticker.transform.copy(rotation = value)) else sticker
                }
            )
        },
        onValueChangeFinished = {
            draft.getOrNull(editIndex)?.transform?.let { latest ->
                val snapped = latest.copy(rotation = latest.rotation.roundToInt().toFloat())
                actions.onUpdateHomeCardStickerTransform(editIndex, snapped)
            }
        },
    )
    MiuixSliderSection(
        title = stringResource(R.string.settings_home_card_sticker_size),
        summary = "${(editTransform.size.coerceIn(HomeCardStickerTransform.SIZE_MIN, sizeMax) * 100).roundToInt()}%",
        value = editTransform.size.coerceIn(HomeCardStickerTransform.SIZE_MIN, sizeMax),
        valueRange = HomeCardStickerTransform.SIZE_MIN..sizeMax,
        onValueChange = { value ->
            onDraftChange(
                draft.mapIndexed { i, sticker ->
                    if (i == editIndex) sticker.copy(transform = sticker.transform.copy(size = value)) else sticker
                }
            )
        },
        onValueChangeFinished = {
            draft.getOrNull(editIndex)?.transform?.let { latest ->
                actions.onUpdateHomeCardStickerTransform(editIndex, latest)
            }
        },
    )
}

/** DETAILS: module description max lines slider (mirrors SukiSU's DETAILS category). */
@Composable
private fun DetailsCardMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val uiState = state.uiState
    var linesValue by remember(uiState.moduleDescriptionMaxLines) {
        mutableIntStateOf(uiState.moduleDescriptionMaxLines)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        MiuixCategoryHeader(ThemeSettingsCategory.DETAILS)
        BasicComponent(
            title = stringResource(R.string.settings_module_description_max_lines),
            summary = stringResource(R.string.settings_module_description_max_lines_summary),
            startAction = {
                MiuixRowIcon(
                    Icons.Rounded.Description,
                    stringResource(R.string.settings_module_description_max_lines),
                )
            },
            endActions = {
                Text(
                    text = "$linesValue " + stringResource(R.string.unit_lines),
                    color = colorScheme.onSurfaceVariantActions,
                )
            },
            bottomAction = {
                Slider(
                    value = linesValue.toFloat(),
                    onValueChange = { linesValue = it.roundToInt() },
                    onValueChangeFinished = { actions.onSetModuleDescriptionMaxLines(linesValue) },
                    valueRange = 1f..5f,
                    showKeyPoints = true,
                    keyPoints = listOf(1f, 2f, 3f, 4f, 5f),
                    magnetThreshold = 0.25f,
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                )
            },
        )
    }
}



package aefox.reyang.ui.screen.colorpalette

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import aefox.reyang.BuildConfig
import aefox.reyang.R
import aefox.reyang.ui.component.HomeCardStickerOverlays
import aefox.reyang.ui.component.bottombar.BottomBarDestination
import aefox.reyang.ui.component.bottombar.BottomBarIconContent
import aefox.reyang.ui.component.bottombar.rememberBottomBarIconPaths
import aefox.reyang.ui.component.material.SegmentedColumn
import aefox.reyang.ui.component.material.SegmentedListItem
import aefox.reyang.ui.component.material.expressiveTopAppBarColors
import aefox.reyang.ui.screen.home.HomeStatusUi
import aefox.reyang.ui.theme.ColorMode
import aefox.reyang.ui.theme.DEFAULT_BACKGROUND_BLUR_PERCENT
import aefox.reyang.ui.theme.DEFAULT_BACKGROUND_SCRIM_PERCENT
import aefox.reyang.ui.theme.LocalCardBackgroundAlpha
import aefox.reyang.ui.theme.LocalColorMode
import aefox.reyang.ui.theme.LocalEnableFloatingBottomBarBlur
import aefox.reyang.ui.theme.LocalHasCustomBackground
import aefox.reyang.ui.theme.LocalThemeVisuals
import aefox.reyang.ui.theme.THEME_BG_BLUR_MAX_RADIUS_DP
import aefox.reyang.ui.theme.appearanceImageColorFilter
import aefox.reyang.ui.theme.backgroundAwareColor
import aefox.reyang.ui.theme.isInDarkTheme
import aefox.reyang.ui.theme.rememberThemeHomeCardStickers
import aefox.reyang.ui.theme.shouldShowHomeCardImage
import aefox.reyang.ui.theme.themeBackgroundFile
import aefox.reyang.ui.theme.themeCustomLabel
import aefox.reyang.ui.theme.themeCustomText
import aefox.reyang.ui.theme.themeHomeCardImageFile
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle
import aefox.reyang.ui.theme.themepackage.ResolvedAppearance
import aefox.reyang.ui.theme.withAppearance
import aefox.reyang.ui.util.drawPainterCover
import java.io.File
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

/**
 * Everything the theme preview renders. It is built from the committed settings, so the
 * preview stays a pure function of the appearance options (mirrors SukiSU's spec,
 * trimmed to the options AeFox ships).
 */
@Immutable
internal data class ThemePreviewSpec(
    val keyColor: Int = 0,
    val isDark: Boolean = false,
    val isAmoled: Boolean = false,
    val miuixMonet: Boolean = false,
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.Default,
    val backgroundPath: String = "",
    val backgroundBlurPercent: Int = DEFAULT_BACKGROUND_BLUR_PERCENT,
    val backgroundScrimPercent: Int = DEFAULT_BACKGROUND_SCRIM_PERCENT,
    val floatingBottomBar: Boolean = true,
    val pageScale: Float = 1f,
    val appearance: ResolvedAppearance? = null,
)

private const val PreviewDesignWidthDp = 360f
private val PreviewBezelShape = RoundedCornerShape(34.dp)
private val PreviewScreenShape = RoundedCornerShape(29.dp)
private val PreviewBezelColor = Color(0xFF141417)

/** One bottom-bar entry of the mock screen (custom labels/icons resolved like the real bar). */
private data class PreviewBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val iconPath: String?,
)

/** Material uses filled icons for the selected tab and outlined ones elsewhere. */
private val MaterialPreviewIcons = listOf(
    Icons.Filled.Home to Icons.Outlined.Home,
    Icons.Filled.Apps to Icons.Outlined.Apps,
    Icons.Filled.Extension to Icons.Outlined.Extension,
    Icons.Filled.Settings to Icons.Outlined.Settings,
)

/** A Miuix-flavoured home screen inside a device frame (mirrors SukiSU ThemePreviewMiuix). */
@Composable
internal fun ThemePreviewMiuix(
    spec: ThemePreviewSpec,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    expanded: Boolean = false,
) {
    val ambientColors = MiuixTheme.colorScheme
    val controller = previewThemeController(spec)
    ThemePreviewLayout(
        modifier = modifier,
        compact = compact,
        expanded = expanded,
        caption = {
            PreviewCaption(
                text = stringResource(R.string.theme_preview_caption_miuix),
                dotColor = ambientColors.primary,
                textColor = ambientColors.onSurfaceVariantSummary,
            )
        },
    ) {
        MiuixTheme(controller = controller) {
            val overlayTokens = spec.appearance?.colorsFor(spec.isDark)
            val themedColors = MiuixTheme.colorScheme.withAppearance(overlayTokens)
            val visuals = LocalThemeVisuals.current
            MiuixTheme(themedColors) {
                val colors = MiuixTheme.colorScheme
                CompositionLocalProvider(
                    LocalColorMode provides if (spec.isDark) ColorMode.DARK.value else ColorMode.LIGHT.value,
                    LocalHasCustomBackground provides spec.backgroundPath.isNotEmpty(),
                    LocalCardBackgroundAlpha provides visuals.cardBackgroundAlphaPercent / 100f,
                ) {
                    PreviewDevice(
                        spec = spec,
                        screenColor = colors.surface,
                        contentColor = colors.onBackground,
                        compact = compact,
                    ) { screenModifier ->
                        MiuixPreviewScreen(spec, screenModifier)
                    }
                }
            }
        }
    }
}

/** The Material 3 Expressive counterpart of [ThemePreviewMiuix]. */
@Composable
internal fun ThemePreviewMaterial(
    spec: ThemePreviewSpec,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    expanded: Boolean = false,
) {
    val ambientColors = MaterialTheme.colorScheme
    val colorScheme = rememberPreviewColorScheme(spec)
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
    ) {
        val visuals = LocalThemeVisuals.current
        CompositionLocalProvider(
            LocalColorMode provides if (spec.isDark) ColorMode.DARK.value else ColorMode.LIGHT.value,
            LocalHasCustomBackground provides spec.backgroundPath.isNotEmpty(),
            LocalCardBackgroundAlpha provides visuals.cardBackgroundAlphaPercent / 100f,
        ) {
            ThemePreviewLayout(
                modifier = modifier,
                compact = compact,
                expanded = expanded,
                caption = {
                    PreviewCaption(
                        text = stringResource(R.string.theme_preview_caption_material),
                        dotColor = ambientColors.primary,
                        textColor = ambientColors.onSurfaceVariant,
                    )
                },
            ) {
                val colors = MaterialTheme.colorScheme
                PreviewDevice(
                    spec = spec,
                    screenColor = colors.surfaceContainer,
                    contentColor = colors.onSurface,
                    compact = compact,
                ) { screenModifier ->
                    MaterialPreviewScreen(spec, screenModifier)
                }
            }
        }
    }
}

/** Builds the nested Miuix theme controller from the spec (same recipe as MiuixTemplateTheme). */
private fun previewThemeController(spec: ThemePreviewSpec): ThemeController {
    val paletteStyle = runCatching {
        ThemePaletteStyle.valueOf(spec.paletteStyle.name)
    }.getOrDefault(ThemePaletteStyle.TonalSpot)
    val colorSpec = if (spec.colorSpec == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }
    val schemeMode = when {
        spec.miuixMonet && spec.isDark -> ColorSchemeMode.MonetDark
        spec.miuixMonet -> ColorSchemeMode.MonetLight
        spec.isDark -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.Light
    }
    return ThemeController(
        schemeMode,
        keyColor = if (spec.miuixMonet && spec.keyColor != 0) Color(spec.keyColor) else null,
        isDark = spec.isDark,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )
}

/** Builds the preview Material color scheme (same recipe as MaterialTemplateTheme). */
@Composable
private fun rememberPreviewColorScheme(spec: ThemePreviewSpec): ColorScheme {
    val context = LocalContext.current
    val scheme = if (spec.keyColor == 0) {
        val system = if (spec.isDark) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
        rememberDynamicColorScheme(
            seedColor = Color.Unspecified,
            isDark = spec.isDark,
            isAmoled = spec.isAmoled,
            style = spec.paletteStyle,
            specVersion = spec.colorSpec,
            primary = system.primary,
            secondary = system.secondary,
            tertiary = system.tertiary,
            neutral = system.surface,
            neutralVariant = system.surfaceVariant,
            error = system.error,
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = Color(spec.keyColor),
            isDark = spec.isDark,
            isAmoled = spec.isAmoled,
            style = spec.paletteStyle,
            specVersion = spec.colorSpec,
        )
    }
    return scheme.withAppearance(spec.appearance?.colorsFor(spec.isDark))
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ThemePreviewLayout(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    expanded: Boolean = false,
    caption: @Composable () -> Unit,
    device: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenRatio = (configuration.screenWidthDp.toFloat() / configuration.screenHeightDp.toFloat())
        .takeIf { it.isFinite() && it > 0f }
        ?.coerceIn(0.42f, 0.62f)
        ?: 0.46f
    val description = stringResource(R.string.theme_preview_description)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val frameWidth = when {
            compact -> minOf(maxWidth * 0.92f, 80.dp)
            expanded -> minOf(maxWidth * 0.82f, 300.dp)
            else -> minOf(maxWidth * 0.52f, 248.dp)
        }
        val frameHeight = frameWidth / screenRatio
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PreviewFrame(frameWidth, frameHeight, compact, description, device)
            if (!compact) {
                Spacer(Modifier.height(10.dp))
                caption()
            }
        }
    }
}

@Composable
private fun PreviewFrame(
    frameWidth: Dp,
    frameHeight: Dp,
    compact: Boolean,
    description: String,
    device: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(frameWidth)
            .height(frameHeight)
            .shadow(if (compact) 12.dp else 22.dp, PreviewBezelShape, clip = false)
            .background(
                Brush.linearGradient(listOf(Color(0xFF242428), PreviewBezelColor)),
                PreviewBezelShape,
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), PreviewBezelShape)
            .padding(if (compact) 3.dp else 5.dp)
            .clip(PreviewScreenShape)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        device()
    }
}

/**
 * Draws the wallpaper (with its blur, appearance filter and scrim) and scales the screen
 * content down to fit the device frame. Scaling through [LocalDensity] keeps text crisp and
 * previews the page-scale option.
 */
@Composable
private fun PreviewDevice(
    spec: ThemePreviewSpec,
    screenColor: Color,
    contentColor: Color,
    compact: Boolean = false,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val baseDensity = LocalDensity.current
        val minScale = if (compact) 0.18f else 0.4f
        val scale = ((maxWidth.value / PreviewDesignWidthDp).coerceIn(minScale, 1f) * spec.pageScale)
            .coerceIn(if (compact) minScale - 0.03f else 0.3f, 1.15f)
        CompositionLocalProvider(
            // Font scale is pinned so an extreme system setting cannot distort this mock.
            LocalDensity provides Density(baseDensity.density * scale, fontScale = 1f),
        ) {
            val backgroundFile = rememberPreviewBackgroundFile(spec)
            // Cards become translucent only while this preview actually paints a wallpaper.
            CompositionLocalProvider(LocalHasCustomBackground provides (backgroundFile != null)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(screenColor)
                ) {
                    PreviewBackgroundImage(spec, backgroundFile)
                    if (backgroundFile != null) {
                        // Soft veil behind the transparent top bars, matching TopBarVeil on the real pages.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(screenColor.copy(alpha = 0.28f), Color.Transparent)
                                    )
                                )
                        )
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        PreviewStatusBar(contentColor)
                        content(Modifier.weight(1f))
                    }
                    PreviewCameraPunch()
                }
            }
        }
    }
}

/**
 * Resolves the background like the real pages do: the explicitly passed preview path wins,
 * then the theme/wallpaper resolver (adaptive variants and the captured system wallpaper).
 */
@Composable
private fun rememberPreviewBackgroundFile(spec: ThemePreviewSpec): File? {
    val context = LocalContext.current
    val visuals = LocalThemeVisuals.current
    val dark = isInDarkTheme()
    return remember(spec.backgroundPath, visuals.adaptive, visuals.useSystemWallpaper, dark) {
        spec.backgroundPath.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?.takeIf { it.isFile }
            ?: themeBackgroundFile(context, visuals.adaptive, dark, visuals.useSystemWallpaper)
    }
}

@Composable
private fun PreviewBackgroundImage(spec: ThemePreviewSpec, file: File?) {
    if (file == null) return
    val context = LocalContext.current
    val blurRadius = (THEME_BG_BLUR_MAX_RADIUS_DP * spec.backgroundBlurPercent / 100f).dp
    // The mtime is part of the cache key so a re-crop that keeps the same path is not stale.
    val stamp = file.lastModified()
    val request = remember(file.absolutePath, stamp) {
        ImageRequest.Builder(context)
            .data(file)
            .memoryCacheKey("${file.absolutePath}@$stamp")
            .diskCacheKey("${file.absolutePath}@$stamp")
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = appearanceImageColorFilter(spec.appearance?.background),
        modifier = Modifier
            .fillMaxSize()
            .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
    )

    val scrimPercent = spec.backgroundScrimPercent
    if (scrimPercent != 0) {
        val scrimColor = if (scrimPercent < 0) {
            Color.Black.copy(alpha = -scrimPercent / 100f)
        } else {
            Color.White.copy(alpha = scrimPercent / 100f)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
        )
    }
}

@Composable
private fun PreviewStatusBar(contentColor: Color) {
    val textStyle = remember(contentColor) {
        TextStyle(
            color = contentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(text = "9:41", style = textStyle)
        Spacer(Modifier.weight(1f))
        PreviewStatusIcon(Icons.Rounded.SignalCellularAlt, contentColor, 8.dp)
        Spacer(Modifier.width(3.dp))
        PreviewStatusIcon(Icons.Rounded.Wifi, contentColor, 8.dp)
        Spacer(Modifier.width(3.dp))
        PreviewStatusIcon(Icons.Rounded.BatteryFull, contentColor, 10.dp)
    }
}

@Composable
private fun PreviewStatusIcon(icon: ImageVector, tint: Color, size: Dp) {
    Image(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
private fun BoxScope.PreviewCameraPunch() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 6.dp)
            .width(52.dp)
            .height(14.dp)
            .background(Color.Black, CircleShape)
    )
}

@Composable
private fun PreviewCaption(
    text: String,
    dotColor: Color,
    textColor: Color,
) {
    val textStyle = remember(textColor) {
        TextStyle(
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(dotColor, CircleShape)
        )
        BasicText(text = text, style = textStyle)
    }
}

/** Adaptive-theme marker (mirrors SukiSU AdaptiveThemeBadge, Miuix flavour). */
@Composable
private fun PreviewAdaptiveBadgeMiuix() {
    val colors = MiuixTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.primaryContainer.copy(alpha = 0.92f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        MiuixText(
            text = stringResource(R.string.theme_adaptive_title),
            fontSize = 9.sp,
            color = colors.onPrimaryContainer,
        )
    }
}

/** Adaptive-theme marker (mirrors SukiSU AdaptiveThemeBadge, Material flavour). */
@Composable
private fun PreviewAdaptiveBadgeMaterial() {
    val colors = MaterialTheme.colorScheme
    MaterialSurface(
        shape = MaterialTheme.shapes.small,
        color = colors.primaryContainer.copy(alpha = 0.92f),
        contentColor = colors.onPrimaryContainer,
    ) {
        MaterialText(
            text = stringResource(R.string.theme_adaptive_title),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Miuix mock screen: mirrors the AeFox Miuix home page structure.
// ---------------------------------------------------------------------------

@Composable
private fun MiuixPreviewScreen(spec: ThemePreviewSpec, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        MiuixPreviewTopBar()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .padding(top = 4.dp, start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (LocalThemeVisuals.current.homeLayout == HomeLayout.IMMERSIVE) {
                MiuixPreviewImmersiveStatus(spec)
            } else {
                MiuixPreviewStatusCard(spec)
            }
            MiuixPreviewInfoCard()
            MiuixPreviewLearnCard()
            Spacer(Modifier.weight(1f))
        }
        MiuixPreviewBottomBar(spec)
    }
}

@Composable
private fun MiuixPreviewTopBar() {
    val colors = MiuixTheme.colorScheme
    val visuals = LocalThemeVisuals.current
    val title = visuals.customHomeTitle.takeIf { it.isNotBlank() }
        ?: themeCustomText("home.title", stringResource(R.string.app_name))
    TopAppBar(
        title = title,
        color = Color.Transparent,
        titleColor = colors.onSurface,
        largeTitleColor = colors.onBackground,
        defaultWindowInsetsPadding = false,
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (visuals.adaptive) {
                    PreviewAdaptiveBadgeMiuix()
                }
                MiuixIcon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = colors.onBackground,
                )
            }
        },
    )
}

/** The working status card of the home page, including the status card image. */
@Composable
private fun MiuixPreviewStatusCard(spec: ThemePreviewSpec) {
    val colors = MiuixTheme.colorScheme
    val visuals = LocalThemeVisuals.current
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val cardImagePainter = cardImageFile?.let { file ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(file)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
        )
    }
    val hasCardImage = cardImageFile != null
    val hideStatusText = hasCardImage && visuals.hideHomeCardStatusText
    val hideDecoration = hasCardImage && visuals.hideHomeCardDecoration
    val stickers = rememberThemeHomeCardStickers()

    val accentColor = if (spec.miuixMonet) colors.primary else Color(0xFF36D167)
    val containerColor = if (spec.miuixMonet) colors.secondaryContainer else Color(0xFFDFFAE4)
    val statusText = themeCustomText(
        "home.status_working",
        stringResource(R.string.module_status_active),
    )

    // SukiSU parity: content-driven card with a 188dp floor; the cover image is cropped.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardDefaults.CornerRadius))
            .then(
                if (cardImagePainter != null) {
                    Modifier.drawPainterCover(cardImagePainter)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.defaultColors(
            color = if (cardImagePainter != null) Color.Transparent else backgroundAwareColor(containerColor),
            contentColor = colors.onSurface,
        ),
    ) {
        BoxWithConstraints {
            if (!hideDecoration) {
                MiuixIcon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 27.dp, y = 31.dp)
                        .size(150.dp),
                    tint = accentColor,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HomeStatusUi.ClassicMinHeight)
                    .padding(22.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (!hideStatusText) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiuixText(
                            text = statusText,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                        )
                        MiuixText(
                            text = BuildConfig.VERSION_DISPLAY,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            HomeCardStickerOverlays(
                stickers = stickers,
                cardWidth = maxWidth,
            )
        }
    }
}

/**
 * Immersive status area: working-status pill plus the optional image banner, positioned and
 * scaled by the package's status alignment/scale/width and pill scale.
 */
@Composable
private fun MiuixPreviewImmersiveStatus(spec: ThemePreviewSpec) {
    val colors = MiuixTheme.colorScheme
    val visuals = LocalThemeVisuals.current
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val scale = HomeStatusStyle.coerceScale(visuals.homeStatusScale)
    val widthFraction = HomeStatusStyle.coerceWidth(visuals.homeStatusWidthFraction)
    val pillScale = HomeStatusStyle.coercePillScale(visuals.homePillScale)
    val hasCustomBackground = LocalHasCustomBackground.current
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val accentColor = if (spec.miuixMonet) colors.primary else Color(0xFF36D167)
    val bannerShape = RoundedCornerShape(HomeStatusUi.BannerCorner)
    val showBanner = visuals.homeCardImageStyle == HomeCardImageStyle.BANNER &&
        shouldShowHomeCardImage(
            cardBackgroundFile = cardImageFile,
            hasCustomBackground = hasCustomBackground,
            showWithoutBackground = visuals.showHomeCardImageWithoutBackground,
        )
    val statusText = themeCustomText(
        "home.status_working",
        stringResource(R.string.module_status_active),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (visuals.homeStatusAlignment == HomeStatusAlignment.END) {
            Alignment.End
        } else {
            Alignment.Start
        },
        verticalArrangement = Arrangement.spacedBy(HomeStatusUi.ClusterGap),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = HomeStatusUi.PillTouchTarget)
                .clip(RoundedCornerShape(50))
                .background(backgroundAwareColor(colors.surfaceContainer, backgroundAlpha = 0.88f))
                .border(
                    1.dp,
                    colors.onSurface.copy(alpha = if (hasCustomBackground) 0.14f else 0.08f),
                    RoundedCornerShape(50),
                )
                .padding(
                    horizontal = HomeStatusUi.pillDp(HomeStatusUi.PillHorizontalPadding, pillScale),
                    vertical = HomeStatusUi.pillDp(8.dp, pillScale),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                HomeStatusUi.pillDp(HomeStatusUi.PillContentGap, pillScale)
            ),
        ) {
            Box(
                modifier = Modifier
                    .size(HomeStatusUi.pillDp(HomeStatusUi.DotSize, pillScale))
                    .clip(CircleShape)
                    .background(accentColor)
            )
            MiuixText(
                text = statusText,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.LabelSizeSp, pillScale),
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
            )
            Box(
                modifier = Modifier
                    .width(HomeStatusUi.pillDp(HomeStatusUi.DividerWidth, pillScale))
                    .height(HomeStatusUi.pillDp(HomeStatusUi.DividerHeight, pillScale))
                    .background(colors.onSurface.copy(alpha = 0.18f))
            )
            MiuixText(
                text = BuildConfig.VERSION_DISPLAY,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.MetaSizeSp, pillScale),
                fontFamily = FontFamily.Monospace,
                color = colors.onSurfaceVariantSummary,
                maxLines = 1,
            )
        }
        if (showBanner && cardImageFile != null) {
            // SukiSU parity: fixed scaled banner height; the image is cover-cropped.
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(widthFraction)) {
                val bannerWidth = maxWidth
                val bannerHeight = HomeStatusUi.scaledDp(HomeStatusUi.BannerHeight, scale)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .clip(bannerShape)
                        .background(colors.surfaceContainer)
                        .border(1.dp, colors.onSurface.copy(alpha = 0.2f), bannerShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(cardImageFile)
                            .crossfade(true)
                            .precision(Precision.EXACT)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High,
                        modifier = Modifier.fillMaxSize(),
                    )
                    HomeCardStickerOverlays(
                        stickers = rememberThemeHomeCardStickers(),
                        cardWidth = bannerWidth,
                        cardHeight = bannerHeight,
                        heightNormalized = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixPreviewInfoCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            PreviewInfoRowMiuix(
                icon = Icons.Filled.Smartphone,
                title = stringResource(R.string.home_device_model),
                content = Build.MODEL,
            )
            PreviewInfoRowMiuix(
                icon = Icons.Filled.Android,
                title = stringResource(R.string.home_android_version),
                content = Build.VERSION.RELEASE,
                bottomPadding = 0.dp,
            )
        }
    }
}

@Composable
private fun PreviewInfoRowMiuix(
    icon: ImageVector,
    title: String,
    content: String,
    bottomPadding: Dp = 24.dp,
) {
    val colors = MiuixTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiuixIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(24.dp),
            tint = colors.onSurface,
        )
        Column(modifier = Modifier.weight(1f)) {
            MiuixText(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
            )
            MiuixText(
                text = content,
                fontSize = 13.sp,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun MiuixPreviewLearnCard() {
    val colors = MiuixTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth()) {
        ArrowPreference(
            title = stringResource(R.string.home_learn_more_title, stringResource(R.string.app_name)),
            summary = stringResource(R.string.home_learn_more_summary),
            startAction = {
                MiuixIcon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp).size(24.dp),
                    tint = colors.onBackground,
                )
            },
            onClick = {},
        )
    }
}

@Composable
private fun rememberMiuixPreviewBarItems(): List<PreviewBarItem> {
    val iconPaths = rememberBottomBarIconPaths()
    return BottomBarDestination.entries.map { destination ->
        PreviewBarItem(
            label = themeCustomLabel(
                destination.textKey,
                stringResource(destination.label),
                destination.retargetNouns,
                destination.aliasTextKey,
            ),
            icon = destination.icon,
            selectedIcon = destination.icon,
            iconPath = iconPaths[destination.slotKey]?.absolutePath,
        )
    }
}

@Composable
private fun MiuixPreviewBottomBar(spec: ThemePreviewSpec) {
    if (spec.floatingBottomBar) {
        MiuixPreviewFloatingBar(spec)
    } else {
        MiuixPreviewNavigationBar()
    }
}

@Composable
private fun MiuixPreviewFloatingBar(spec: ThemePreviewSpec) {
    val colors = MiuixTheme.colorScheme
    val items = rememberMiuixPreviewBarItems()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        MiuixSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = if (LocalEnableFloatingBottomBarBlur.current) {
                colors.surfaceContainer.copy(alpha = 0.4f)
            } else {
                colors.surfaceContainer
            },
            contentColor = colors.onSurface,
            border = BorderStroke(0.5.dp, colors.onSurface.copy(alpha = 0.08f)),
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == 0
                    Row(
                        modifier = Modifier
                            .then(
                                if (selected) {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(colors.onSurface.copy(alpha = 0.12f))
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BottomBarIconContent(
                            icon = item.icon,
                            iconPath = item.iconPath,
                            contentDescription = item.label,
                            tint = colors.onSurface,
                            size = 14.dp,
                        )
                        MiuixText(
                            text = item.label,
                            fontSize = 9.sp,
                            color = colors.onSurface,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixPreviewNavigationBar() {
    val colors = MiuixTheme.colorScheme
    val items = rememberMiuixPreviewBarItems()
    // Neutral item colors: selection only changes the indicator, never the accent.
    val itemColors = NavigationBarDefaults.navigationBarItemColors(
        selectedContentColor = colors.onSurfaceContainer,
        unselectedContentColor = colors.onSurfaceContainer,
    )
    NavigationBar(
        color = backgroundAwareColor(colors.surface),
        showDivider = true,
        defaultWindowInsetsPadding = false,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == 0
            if (item.iconPath.isNullOrBlank()) {
                NavigationBarItem(
                    modifier = Modifier.weight(1f),
                    icon = item.icon,
                    label = item.label,
                    selected = selected,
                    onClick = {},
                    colors = itemColors,
                )
            } else {
                PreviewMiuixNavigationBarItem(item, selected = selected)
            }
        }
    }
}

/** Neutral item matching the real bar: selection only changes opacity, never the accent. */
@Composable
private fun RowScope.PreviewMiuixNavigationBarItem(
    item: PreviewBarItem,
    selected: Boolean,
) {
    val colors = MiuixTheme.colorScheme
    val tint = colors.onSurfaceContainer.copy(
        alpha = if (selected) 1f else NavigationBarDefaults.UnselectedAlpha,
    )
    Column(
        modifier = Modifier
            .height(NavigationBarDefaults.ItemHeight)
            .weight(1f)
            .selectable(
                selected = selected,
                onClick = {},
                role = Role.Tab,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(modifier = Modifier.padding(top = NavigationBarDefaults.IconTopPadding)) {
            BottomBarIconContent(
                icon = item.icon,
                iconPath = item.iconPath,
                contentDescription = null,
                tint = tint,
                size = NavigationBarDefaults.IconSize,
            )
        }
        MiuixText(
            text = item.label,
            color = tint,
            fontSize = NavigationBarDefaults.LabelFontSize,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(bottom = NavigationBarDefaults.BottomPadding),
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Material mock screen: mirrors the AeFox Material home page structure.
// ---------------------------------------------------------------------------

@Composable
private fun MaterialPreviewScreen(spec: ThemePreviewSpec, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        MaterialPreviewTopBar()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .padding(top = 4.dp, start = 14.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (LocalThemeVisuals.current.homeLayout == HomeLayout.IMMERSIVE) {
                MaterialPreviewImmersiveStatus()
            } else {
                MaterialPreviewStatusCard()
            }
            MaterialPreviewInfoCard()
            MaterialPreviewLearnCard()
            Spacer(Modifier.weight(1f))
        }
        MaterialPreviewBottomBar(spec)
    }
}

@Composable
private fun MaterialPreviewTopBar() {
    val visuals = LocalThemeVisuals.current
    val title = visuals.customHomeTitle.takeIf { it.isNotBlank() }
        ?: themeCustomText("home.title", stringResource(R.string.app_name))
    LargeFlexibleTopAppBar(
        title = { MaterialText(title) },
        expandedHeight = 104.dp,
        collapsedHeight = 56.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = expressiveTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (visuals.adaptive) {
                    PreviewAdaptiveBadgeMaterial()
                }
                MaterialIcon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}

/** The working status card of the home page, including the status card image. */
@Composable
private fun MaterialPreviewStatusCard() {
    val colors = MaterialTheme.colorScheme
    val visuals = LocalThemeVisuals.current
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val cardImagePainter = cardImageFile?.let { file ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(file)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
        )
    }
    val hasCardImage = cardImageFile != null
    val hideStatusText = hasCardImage && visuals.hideHomeCardStatusText
    val hideDecoration = hasCardImage && visuals.hideHomeCardDecoration
    val stickers = rememberThemeHomeCardStickers()
    val containerColor = colors.secondaryContainer
    val contentColor = colors.onSecondaryContainer
    val statusText = themeCustomText(
        "home.status_working",
        stringResource(R.string.module_status_active),
    )

    // SukiSU parity: content-driven card with a 188dp floor; the cover image is cropped.
    MaterialSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(
                if (cardImagePainter != null) {
                    Modifier.drawPainterCover(cardImagePainter)
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.large,
        color = if (cardImagePainter != null) Color.Transparent else backgroundAwareColor(containerColor),
        contentColor = contentColor,
    ) {
        BoxWithConstraints(
            modifier = if (hideDecoration) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawStatusRings(contentColor.copy(alpha = 0.18f)) }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HomeStatusUi.ClassicMinHeight)
                    .padding(22.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (!hideStatusText) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MaterialText(
                            text = statusText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                        )
                        MaterialText(
                            text = BuildConfig.VERSION_DISPLAY,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
            HomeCardStickerOverlays(
                stickers = stickers,
                cardWidth = maxWidth,
            )
        }
    }
}

/** Classic-card decoration rings, drawn behind the information block. */
private fun DrawScope.drawStatusRings(color: Color) {
    val center = Offset(size.width - 36.dp.toPx(), 32.dp.toPx())
    val stroke = 2.dp.toPx()
    drawCircle(color = color, radius = 112.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(
        color = color,
        radius = 84.dp.toPx(),
        center = center,
        style = Stroke(
            stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 8.dp.toPx())),
        ),
    )
    drawCircle(color = color, radius = 56.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(color = color, radius = 18.dp.toPx(), center = center)
}

/** Immersive status area: working-status pill plus the optional image banner. */
@Composable
private fun MaterialPreviewImmersiveStatus() {
    val colors = MaterialTheme.colorScheme
    val visuals = LocalThemeVisuals.current
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val scale = HomeStatusStyle.coerceScale(visuals.homeStatusScale)
    val widthFraction = HomeStatusStyle.coerceWidth(visuals.homeStatusWidthFraction)
    val pillScale = HomeStatusStyle.coercePillScale(visuals.homePillScale)
    val hasCustomBackground = LocalHasCustomBackground.current
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val bannerShape = RoundedCornerShape(HomeStatusUi.BannerCorner)
    val showBanner = visuals.homeCardImageStyle == HomeCardImageStyle.BANNER &&
        shouldShowHomeCardImage(
            cardBackgroundFile = cardImageFile,
            hasCustomBackground = hasCustomBackground,
            showWithoutBackground = visuals.showHomeCardImageWithoutBackground,
        )
    val statusText = themeCustomText(
        "home.status_working",
        stringResource(R.string.module_status_active),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (visuals.homeStatusAlignment == HomeStatusAlignment.END) {
            Alignment.End
        } else {
            Alignment.Start
        },
        verticalArrangement = Arrangement.spacedBy(HomeStatusUi.ClusterGap),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = HomeStatusUi.PillTouchTarget)
                .clip(RoundedCornerShape(50))
                .background(backgroundAwareColor(colors.surfaceContainer, backgroundAlpha = 0.88f))
                .border(
                    1.dp,
                    colors.outline.copy(alpha = 0.18f),
                    RoundedCornerShape(50),
                )
                .padding(
                    horizontal = HomeStatusUi.pillDp(HomeStatusUi.PillHorizontalPadding, pillScale),
                    vertical = HomeStatusUi.pillDp(8.dp, pillScale),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                HomeStatusUi.pillDp(HomeStatusUi.PillContentGap, pillScale)
            ),
        ) {
            Box(
                modifier = Modifier
                    .size(HomeStatusUi.pillDp(HomeStatusUi.DotSize, pillScale))
                    .clip(CircleShape)
                    .background(colors.primary)
            )
            MaterialText(
                text = statusText,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.LabelSizeSp, pillScale),
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
            )
            Box(
                modifier = Modifier
                    .width(HomeStatusUi.pillDp(HomeStatusUi.DividerWidth, pillScale))
                    .height(HomeStatusUi.pillDp(HomeStatusUi.DividerHeight, pillScale))
                    .background(colors.onSurface.copy(alpha = 0.18f))
            )
            MaterialText(
                text = BuildConfig.VERSION_DISPLAY,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.MetaSizeSp, pillScale),
                fontFamily = FontFamily.Monospace,
                color = colors.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (showBanner && cardImageFile != null) {
            // SukiSU parity: fixed scaled banner height; the image is cover-cropped.
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(widthFraction)) {
                val bannerWidth = maxWidth
                val bannerHeight = HomeStatusUi.scaledDp(HomeStatusUi.BannerHeight, scale)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .clip(bannerShape)
                        .background(colors.surfaceContainer)
                        .border(1.dp, colors.outline.copy(alpha = 0.24f), bannerShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(cardImageFile)
                            .crossfade(true)
                            .precision(Precision.EXACT)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High,
                        modifier = Modifier.fillMaxSize(),
                    )
                    HomeCardStickerOverlays(
                        stickers = rememberThemeHomeCardStickers(),
                        cardWidth = bannerWidth,
                        cardHeight = bannerHeight,
                        heightNormalized = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialPreviewInfoCard() {
    val colors = MaterialTheme.colorScheme
    SegmentedColumn(
        modifier = Modifier.fillMaxWidth(),
        content = listOf(
            {
                SegmentedListItem(
                    headlineContent = {
                        MaterialText(
                            text = stringResource(R.string.home_device_model),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingContent = { MaterialIcon(Icons.Filled.Smartphone, contentDescription = null) },
                    supportingContent = {
                        MaterialText(
                            text = Build.MODEL,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    },
                )
            },
            {
                SegmentedListItem(
                    headlineContent = {
                        MaterialText(
                            text = stringResource(R.string.home_android_version),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingContent = { MaterialIcon(Icons.Filled.Android, contentDescription = null) },
                    supportingContent = {
                        MaterialText(
                            text = Build.VERSION.RELEASE,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    },
                )
            },
        ),
    )
}

@Composable
private fun MaterialPreviewLearnCard() {
    SegmentedColumn(
        modifier = Modifier.fillMaxWidth(),
        content = listOf(
            {
                SegmentedListItem(
                    headlineContent = {
                        MaterialText(
                            stringResource(
                                R.string.home_learn_more_title,
                                stringResource(R.string.app_name),
                            )
                        )
                    },
                    supportingContent = {
                        MaterialText(stringResource(R.string.home_learn_more_summary))
                    },
                    leadingContent = {
                        MaterialIcon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                    },
                    trailingContent = {
                        MaterialIcon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    },
                )
            },
        ),
    )
}

@Composable
private fun rememberMaterialPreviewBarItems(): List<PreviewBarItem> {
    val iconPaths = rememberBottomBarIconPaths()
    return BottomBarDestination.entries.mapIndexed { index, destination ->
        val (selectedIcon, unselectedIcon) = MaterialPreviewIcons[index]
        PreviewBarItem(
            label = themeCustomLabel(
                destination.textKey,
                stringResource(destination.label),
                destination.retargetNouns,
                destination.aliasTextKey,
            ),
            icon = unselectedIcon,
            selectedIcon = selectedIcon,
            iconPath = iconPaths[destination.slotKey]?.absolutePath,
        )
    }
}

@Composable
private fun MaterialPreviewBottomBar(spec: ThemePreviewSpec) {
    val colors = MaterialTheme.colorScheme
    val items = rememberMaterialPreviewBarItems()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        MaterialSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = backgroundAwareColor(colors.surfaceContainerHighest),
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == 0
                    Row(
                        modifier = Modifier
                            .then(
                                if (selected) {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(colors.onSurface.copy(alpha = 0.08f))
                                        .padding(horizontal = 12.dp)
                                } else {
                                    Modifier.padding(horizontal = 10.dp)
                                }
                            )
                            .height(38.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BottomBarIconContent(
                            icon = if (selected) item.selectedIcon else item.icon,
                            iconPath = item.iconPath,
                            contentDescription = item.label,
                            tint = if (selected) colors.onSurface else colors.onSurfaceVariant,
                            size = 18.dp,
                        )
                        if (selected) {
                            MaterialText(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Small rounded color tile behind a leading icon (mirrors SukiSU IconTile). */
@Composable
internal fun MaterialIconTile(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        MaterialIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

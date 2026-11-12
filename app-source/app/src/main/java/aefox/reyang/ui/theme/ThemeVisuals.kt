package aefox.reyang.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import aefox.reyang.data.repository.SettingsRepository
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.theme.themepackage.AppearanceOverlayStore
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeCardSticker
import aefox.reyang.ui.theme.themepackage.HomeCardStickerTransform
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle
import aefox.reyang.ui.theme.themepackage.ResolvedAppearance
import aefox.reyang.ui.theme.themepackage.ThemeImageStore
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

/**
 * Render-side snapshot of the theme package appearance: carries only pure
 * data fields relevant to drawing.
 *
 * Images never enter the snapshot: adaptive packages switch images between
 * light/dark variant dirs following the current theme, so all image reads
 * are resolved during composition via the helpers at the bottom of this file.
 */
data class ThemeVisuals(
    val adaptive: Boolean = false,
    val backgroundBlurPercent: Int = DEFAULT_BACKGROUND_BLUR_PERCENT,
    val backgroundScrimPercent: Int = DEFAULT_BACKGROUND_SCRIM_PERCENT,
    val cardBackgroundAlphaPercent: Int = DEFAULT_CARD_ALPHA_PERCENT,
    val homeLayout: HomeLayout = HomeLayout.CLASSIC,
    val homeStatusAlignment: HomeStatusAlignment = HomeStatusAlignment.START,
    val homeStatusScale: Float = HomeStatusStyle.SCALE_DEFAULT,
    val homePillScale: Float = HomeStatusStyle.PILL_SCALE_DEFAULT,
    val homeStatusWidthFraction: Float = HomeStatusStyle.WIDTH_DEFAULT,
    val hideHomeCardStatusText: Boolean = false,
    val hideHomeCardDecoration: Boolean = false,
    val homeCardImageStyle: HomeCardImageStyle = HomeCardImageStyle.BANNER,
    val showHomeCardImageWithoutBackground: Boolean = false,
    val customHomeTitle: String = "",
    val customTexts: Map<String, String> = emptyMap(),
    val stickerTransforms: List<HomeCardStickerTransform> = emptyList(),
    val bottomBarIcons: Map<String, String> = emptyMap(),
    /** System wallpaper happens to be the global background (captured copy). */
    val useSystemWallpaper: Boolean = false,
    /** Static appearance overlay (resolved); null when JSON is missing/invalid, consumers fall back to current colors. */
    val appearance: ResolvedAppearance? = null,
)

/** Defaults kept in sync with ThemePackageManager (mirrors SukiSU ImageStorage constants). */
const val DEFAULT_BACKGROUND_BLUR_PERCENT = 50
const val DEFAULT_BACKGROUND_SCRIM_PERCENT = -35
const val DEFAULT_CARD_ALPHA_PERCENT = 60

/** Max physical blur radius (dp) for the background; actual radius = MAX * blurPercent / 100. */
const val THEME_BG_BLUR_MAX_RADIUS_DP = 40f

val LocalThemeVisuals = staticCompositionLocalOf { ThemeVisuals() }

/**
 * Sampling layer for the global background image: with a custom background,
 * Miuix frosted bars need the background layer composited beneath the page
 * sampling layer, otherwise transparent pixels blur into black. Null when
 * no background is set.
 */
val LocalAppBackgroundBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/** Builds the render snapshot from the settings repo; layout/enum fields are normalized once so the Compose tree only reads valid values. */
fun themeVisualsFrom(repo: SettingsRepository): ThemeVisuals = ThemeVisuals(
    adaptive = repo.themeAdaptive,
    backgroundBlurPercent = repo.backgroundBlurPercent,
    backgroundScrimPercent = repo.backgroundScrimPercent,
    cardBackgroundAlphaPercent = repo.cardBackgroundAlphaPercent,
    homeLayout = HomeLayout.fromValue(repo.homeLayout),
    homeStatusAlignment = HomeStatusAlignment.fromValue(repo.homeStatusAlignment),
    homeStatusScale = HomeStatusStyle.coerceScale(repo.homeStatusScale),
    homePillScale = HomeStatusStyle.coercePillScale(repo.homePillScale),
    homeStatusWidthFraction = HomeStatusStyle.coerceWidth(repo.homeStatusWidthFraction),
    hideHomeCardStatusText = repo.hideHomeCardStatusText,
    hideHomeCardDecoration = repo.hideHomeCardDecoration,
    homeCardImageStyle = HomeCardImageStyle.fromValue(repo.homeCardImageStyle),
    showHomeCardImageWithoutBackground = repo.showHomeCardImageWithoutBackground,
    customHomeTitle = repo.customHomeTitle,
    customTexts = repo.customTexts,
    stickerTransforms = repo.stickerTransforms,
    bottomBarIcons = repo.bottomBarIconPaths,
    useSystemWallpaper = repo.useSystemWallpaper,
    appearance = resolvedAppearanceFrom(repo.themeAppearanceJson),
)

/** Parses the static overlay JSON; returns null on blank/failure/limit-exceeded (consumers fall back to current colors, no crash). */
private fun resolvedAppearanceFrom(raw: String?): ResolvedAppearance? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        AppearanceOverlayStore.parse(raw).getOrThrow().resolved
    }.getOrNull()
}

// ---------------------------------------------------------------------------
// Unified image resolution: adaptive -> pick the light/dark variant by
// isInDarkTheme(); otherwise the top-level path. Returns null on invalid
// path / missing file (fall back to no image); callers must not assume
// non-null.
// ---------------------------------------------------------------------------

/** Global background image; null when missing. The captured system wallpaper wins when active. */
fun themeBackgroundFile(
    context: Context,
    adaptive: Boolean,
    dark: Boolean,
    useSystemWallpaper: Boolean = false,
): File? {
    if (useSystemWallpaper) {
        val wallpaper = ThemeImageStore.systemWallpaperFile(context).takeIf { it.isFile }
        if (wallpaper != null) return wallpaper
    }
    val file = if (adaptive) {
        ThemeImageStore.backgroundFile(context, dark)
    } else {
        ThemeImageStore.backgroundFile(context, null)
    }
    return file.takeIf { it.isFile }
}

/**
 * Independent home-card (immersive banner) image: user-picked card image first,
 * theme package card background as the fallback so existing images keep rendering.
 */
fun themeHomeCardImageFile(context: Context, adaptive: Boolean, dark: Boolean): File? {
    val picked = if (adaptive) {
        ThemeImageStore.homeCardImageFile(context, dark)
    } else {
        ThemeImageStore.homeCardImageFile(context, null)
    }
    return picked.takeIf { it.isFile } ?: themeCardBackgroundFile(context, adaptive, dark)
}

/** Status card background image (card_background); null when missing. */
fun themeCardBackgroundFile(context: Context, adaptive: Boolean, dark: Boolean): File? {
    val file = if (adaptive) {
        ThemeImageStore.cardBackgroundFile(context, dark)
    } else {
        ThemeImageStore.cardBackgroundFile(context, null)
    }
    return file.takeIf { it.isFile }
}

/** Sticker file at the given index; null when missing. */
fun themeStickerFile(context: Context, adaptive: Boolean, dark: Boolean, index: Int): File? {
    val file = if (adaptive) {
        ThemeImageStore.stickerFile(context, index, dark)
    } else {
        ThemeImageStore.stickerFile(context, index, null)
    }
    return file.takeIf { it.isFile }
}

/** All existing sticker files (in 0..MAX_COUNT order). */
fun themeStickerFiles(context: Context, adaptive: Boolean, dark: Boolean): List<File> =
    (0 until HomeCardSticker.MAX_COUNT).mapNotNull {
        themeStickerFile(context, adaptive, dark, it)
    }

/** Reads the active background file during composition (adaptive follows light/dark). */
@Composable
fun rememberThemeBackgroundFile(): File? {
    val context = LocalContext.current
    val visuals = LocalThemeVisuals.current
    val dark = isInDarkTheme()
    return themeBackgroundFile(context, visuals.adaptive, dark, visuals.useSystemWallpaper)
}

/** Whether a global background image is active, resolved during composition. */
@Composable
fun rememberThemeBackgroundActive(): Boolean = rememberThemeBackgroundFile() != null

/**
 * Root scaffold container color shared by all pages: transparent while a theme
 * package background is active so the global background layer (drawn in
 * MainActivity) shows through, otherwise the opaque surface color of the
 * active UI mode. Mirrors SukiSU AppScaffold. Card and list item colors stay
 * untouched; bars combine this with BlurredBar / top bar colors separately.
 */
@Composable
fun rememberAppContainerColor(): Color {
    if (rememberThemeBackgroundActive()) return Color.Transparent
    return when (LocalUiMode.current) {
        UiMode.Material -> MaterialTheme.colorScheme.surface
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
}

/**
 * Assembles the status card sticker list during composition: files resolve by
 * the current light/dark theme, transforms pair by the same index (defaults
 * when missing).
 */
@Composable
fun rememberThemeHomeCardStickers(): List<HomeCardSticker> {
    val context = LocalContext.current
    val visuals = LocalThemeVisuals.current
    val dark = isInDarkTheme()
    val adaptive = visuals.adaptive
    return remember(visuals.stickerTransforms, adaptive, dark) {
        themeStickerFiles(context, adaptive, dark).mapIndexed { index, file ->
            HomeCardSticker(
                path = file.absolutePath,
                transform = visuals.stickerTransforms.getOrNull(index)?.coerced()
                    ?: HomeCardStickerTransform(),
            )
        }
    }
}

/**
 * Whether the card image participates in rendering: an image exists and
 * (a global background is set, or explicitly shown without background).
 * Same semantics as SukiSU's shouldShowHomeCardImage.
 */
fun shouldShowHomeCardImage(
    cardBackgroundFile: File?,
    hasCustomBackground: Boolean,
    showWithoutBackground: Boolean,
): Boolean = cardBackgroundFile != null && (hasCustomBackground || showWithoutBackground)

/**
 * Custom text replacement: falls back to [default] when no key has a custom
 * value. [fallbackKeys] let a page inherit text authored for a different key in
 * a third-party theme package.
 */
@Composable
fun themeCustomText(key: String?, default: String, vararg fallbackKeys: String?): String {
    val texts = LocalThemeVisuals.current.customTexts
    if (key != null) {
        texts[key]?.takeIf { it.isNotBlank() }?.let { return it }
    }
    fallbackKeys.forEach { alias ->
        if (alias != null) {
            texts[alias]?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }
    return default
}

/** Placeholders filled at runtime, e.g. `%s`, `%1$s` or `%d`. */
private val CustomTextFormatSpecifier = Regex("""%(?:\d+\$)?[a-zA-Z]""")

/**
 * Splits a template into the literal pieces around its placeholders.
 *
 * A template with fewer placeholders than [argCount] is treated as the first
 * piece with empty trailing pieces, so the editor still shows one field per arg.
 */
fun splitCustomTextTemplate(template: String, argCount: Int): List<String> {
    if (argCount <= 0) return listOf(template)
    val matches = CustomTextFormatSpecifier.findAll(template).take(argCount).toList()
    if (matches.size < argCount) return listOf(template) + List(argCount) { "" }
    val parts = ArrayList<String>(argCount + 1)
    var start = 0
    matches.forEach { match ->
        parts += template.substring(start, match.range.first)
        start = match.range.last + 1
    }
    parts += template.substring(start)
    return parts
}

/** Joins edited literal pieces back into a canonical `%s` template. */
fun joinCustomTextTemplate(parts: List<String>, argCount: Int): String {
    if (argCount <= 0) return parts.firstOrNull().orEmpty()
    if (parts.size != argCount + 1) return parts.joinToString("")
    return buildString {
        parts.forEachIndexed { index, part ->
            append(part)
            if (index < argCount) append("%s")
        }
    }
}

/** Fills a template with runtime args; missing placeholders append the values instead. */
fun formatCustomText(template: String, args: List<Any>): String {
    if (args.isEmpty()) return template
    if (!CustomTextFormatSpecifier.containsMatchIn(template)) {
        val values = args.joinToString(" ")
        return if (template.isBlank()) values else template.trimEnd() + " " + values
    }
    return try {
        String.format(template, *args.toTypedArray())
    } catch (_: IllegalArgumentException) {
        template
    }
}

/** Page nouns a third-party theme package may have renamed instead of our tab name. */
val SukiSuSuperuserNouns = listOf("超级用户", "超级使用者", "权限", "授权", "Superuser", "SuperUser", "Root")

/**
 * Re-target a tab label authored for a third-party theme package: the package may
 * have renamed a page noun, and our tab keeps its own noun while inheriting the
 * decoration text.
 */
fun retargetTabLabel(raw: String?, default: String, sourceNouns: List<String>): String {
    if (raw.isNullOrBlank()) return default
    sourceNouns.forEach { noun ->
        val index = raw.indexOf(noun, ignoreCase = true)
        if (index >= 0) return raw.replaceRange(index, index + noun.length, default)
    }
    return raw
}

/**
 * [themeCustomText] with SukiSU page-name re-targeting: the package value is
 * first resolved (canonical key, then aliases) and its SukiSU page noun is
 * replaced with our own default label.
 */
@Composable
fun themeCustomLabel(
    key: String?,
    default: String,
    sourceNouns: List<String>,
    vararg fallbackKeys: String?,
): String {
    val texts = LocalThemeVisuals.current.customTexts
    val raw = sequenceOf(key)
        .plus(fallbackKeys.asSequence())
        .filterNotNull()
        .mapNotNull { texts[it]?.takeIf(String::isNotBlank) }
        .firstOrNull()
    return retargetTabLabel(raw, default, sourceNouns)
}

package aefox.reyang.ui.theme.themepackage

import android.content.Context
import android.content.SharedPreferences
import aefox.reyang.aefoxApp
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Shared tolerant parser for the sticker-transform list persisted in preferences. */
private val stickerTransformsJson = Json { ignoreUnknownKeys = true }

/**
 * Adaptive theme builder draft: one captured variant per system mode.
 *
 * Capturing stores the current appearance fields plus a copy of the current
 * background/card/sticker images under `filesDir/theme/draft/{light,dark}`.
 * Applying promotes both variants into the managed `light`/`dark` directories
 * and switches the manager into adaptive mode, so the existing renderer picks
 * the variant that matches the system mode.
 */
@Serializable
data class AdaptiveThemeDraft(
    val light: ThemeVariantData? = null,
    val dark: ThemeVariantData? = null,
) {
    val isComplete: Boolean get() = light != null && dark != null
}

/** JSON storage for [AdaptiveThemeDraft] (persisted in `adaptiveDraftJson`). */
@OptIn(ExperimentalSerializationApi::class)
object AdaptiveThemeDraftStore {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun read(raw: String?): AdaptiveThemeDraft {
        if (raw.isNullOrBlank()) return AdaptiveThemeDraft()
        return runCatching {
            json.decodeFromString(AdaptiveThemeDraft.serializer(), raw)
        }.getOrDefault(AdaptiveThemeDraft())
    }

    fun encode(draft: AdaptiveThemeDraft): String =
        json.encodeToString(AdaptiveThemeDraft.serializer(), draft)
}

/**
 * Capture/apply/clear operations for the adaptive builder. All heavy work runs
 * on the IO dispatcher; the caller only refreshes its UI state afterwards.
 */
object AdaptiveThemeBuilder {
    private const val BACKGROUND_NAME = "background"
    private const val CARD_BACKGROUND_NAME = "card_background"
    private const val STICKER_PREFIX = "card_sticker_"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private fun prefs(): SharedPreferences =
        aefoxApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Draft read from prefs (images are already on disk when present). */
    fun readDraft(): AdaptiveThemeDraft =
        AdaptiveThemeDraftStore.read(prefs().getString(ThemePrefKeys.THEME_ADAPTIVE_DRAFT, null))

    /** Capture the current appearance as the light or dark variant. */
    suspend fun capture(context: Context, dark: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val prefs = prefs()
                val variant = currentVariant(prefs)
                val draftDir = ThemeImageStore.draftVariantDir(context, dark)
                draftDir.mkdirs()
                val adaptive = prefs.getBoolean(ThemePrefKeys.THEME_ADAPTIVE, false)
                copyVariantImages(context, dark, adaptive)
                ThemeImageStore.writeVariantDataAt(draftDir, variant)
                val draft = readDraft()
                val updated = if (dark) draft.copy(dark = variant) else draft.copy(light = variant)
                prefs.edit {
                    putString(ThemePrefKeys.THEME_ADAPTIVE_DRAFT, AdaptiveThemeDraftStore.encode(updated))
                }
            }
        }

    /** Promote both captured variants and switch the manager into adaptive mode. */
    suspend fun applyDraft(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val draft = readDraft()
            require(draft.isComplete) { "adaptive draft is incomplete" }
            listOf(false, true).forEach { dark ->
                promoteDraft(context, dark)
                val target = ThemeImageStore.variantDir(context, dark)
                ThemeImageStore.writeVariantData(
                    context,
                    dark,
                    (if (dark) draft.dark else draft.light)!!,
                )
                require(target.isDirectory) { "variant directory missing" }
            }
            val prefs = prefs()
            val dark = currentDarkMode(prefs)
            val active = if (dark) draft.dark!! else draft.light!!
            writeVariantToPrefs(prefs, active, dark)
            prefs.edit {
                putBoolean(ThemePrefKeys.THEME_ADAPTIVE, true)
                putString(
                    ThemePrefKeys.THEME_LIGHT_DIR,
                    ThemeImageStore.variantDir(context, false).absolutePath,
                )
                putString(
                    ThemePrefKeys.THEME_DARK_DIR,
                    ThemeImageStore.variantDir(context, true).absolutePath,
                )
                putInt(ThemePrefKeys.THEME_ADAPTIVE_APPLIED_MODE, if (dark) 1 else 0)
            }
            ThemeImageStore.clearDraft(context)
            prefs.edit { putString(ThemePrefKeys.THEME_ADAPTIVE_DRAFT, "") }
        }
    }

    /** Discard the draft and its staged images. */
    suspend fun clearDraft(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            ThemeImageStore.clearDraft(context)
            prefs().edit { putString(ThemePrefKeys.THEME_ADAPTIVE_DRAFT, "") }
        }
    }

    private fun currentVariant(prefs: SharedPreferences): ThemeVariantData {
        val stickerTransforms = prefs.getString(ThemePrefKeys.THEME_STICKER_TRANSFORMS, null)
            ?.let {
                runCatching {
                    stickerTransformsJson
                        .decodeFromString<List<HomeCardStickerTransform>>(it)
                }.getOrNull()
            }
            .orEmpty()
        return ThemeVariantData(
            themeMode = prefs.getInt(ThemePrefKeys.COLOR_MODE, 0),
            miuixMonet = prefs.getBoolean(ThemePrefKeys.MIUIX_MONET, false),
            keyColor = prefs.getInt(ThemePrefKeys.KEY_COLOR, 0),
            colorStyle = prefs.getString(ThemePrefKeys.COLOR_STYLE, null),
            colorSpec = prefs.getString(ThemePrefKeys.COLOR_SPEC, null),
            cardBackgroundAlphaPercent = prefs.getInt(ThemePrefKeys.THEME_CARD_BG_ALPHA_PERCENT, 60),
            customBackgroundBlurPercent = prefs.getInt(ThemePrefKeys.THEME_BG_BLUR_PERCENT, 50),
            customBackgroundScrimPercent = prefs.getInt(ThemePrefKeys.THEME_BG_SCRIM_PERCENT, -35),
            hasBackground = prefs.getBoolean(ThemePrefKeys.THEME_HAS_BACKGROUND, false),
            hasCardBackground = prefs.getBoolean(ThemePrefKeys.THEME_HAS_CARD_BACKGROUND, false),
            hideHomeCardStatusText = prefs.getBoolean(
                ThemePrefKeys.THEME_HIDE_HOME_CARD_STATUS_TEXT,
                false,
            ),
            hideHomeCardDecoration = prefs.getBoolean(
                ThemePrefKeys.THEME_HIDE_HOME_CARD_DECORATION,
                false,
            ),
            homeCardStickerTransforms = stickerTransforms,
            homeLayout = prefs.getString(ThemePrefKeys.THEME_HOME_LAYOUT, null),
            homeStatusAlignment = prefs.getString(ThemePrefKeys.THEME_HOME_STATUS_ALIGNMENT, null),
            homeStatusScale = prefs.getFloat(ThemePrefKeys.THEME_HOME_STATUS_SCALE, 1f),
            homePillScale = prefs.getFloat(ThemePrefKeys.THEME_HOME_PILL_SCALE, 1f),
            homeStatusWidthFraction = prefs.getFloat(
                ThemePrefKeys.THEME_HOME_STATUS_WIDTH_FRACTION,
                1f,
            ),
            homeCardImageStyle = prefs.getString(ThemePrefKeys.THEME_HOME_CARD_IMAGE_STYLE, null),
            showHomeCardImageWithoutBackground = prefs.getBoolean(
                ThemePrefKeys.THEME_SHOW_HOME_CARD_IMAGE_WITHOUT_BACKGROUND,
                false,
            ),
            uiMode = prefs.getString(ThemePrefKeys.UI_MODE, null),
            enableFloatingBottomBar = prefs.getBoolean(
                ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR,
                true,
            ),
            enableFloatingBottomBarBlur = prefs.getBoolean(
                ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR_BLUR,
                true,
            ),
            enableNavigationBadge = prefs.getBoolean(
                ThemePrefKeys.THEME_ENABLE_NAVIGATION_BADGE,
                true,
            ),
            enablePredictiveBack = prefs.getBoolean(ThemePrefKeys.ENABLE_PREDICTIVE_BACK, true),
            enableSwipeDismiss = prefs.getBoolean(ThemePrefKeys.THEME_ENABLE_SWIPE_DISMISS, true),
            enableBlur = prefs.getBoolean(ThemePrefKeys.ENABLE_BLUR, true),
            enableProgressiveBlur = prefs.getBoolean(
                ThemePrefKeys.THEME_ENABLE_PROGRESSIVE_BLUR,
                false,
            ),
            pagerInterceptionMode = prefs.getInt(ThemePrefKeys.THEME_PAGER_INTERCEPTION_MODE, 1),
            pageScale = prefs.getFloat(ThemePrefKeys.THEME_PAGE_SCALE, 1f),
            moduleDescriptionMaxLines = prefs.getInt(
                ThemePrefKeys.THEME_MODULE_DESCRIPTION_MAX_LINES,
                4,
            ),
        )
    }

    private fun copyVariantImages(context: Context, dark: Boolean, adaptive: Boolean) {
        val sourceDir = if (adaptive) {
            ThemeImageStore.variantDir(context, dark)
        } else {
            ThemeImageStore.themeDir(context)
        }
        copyIfExists(
            File(sourceDir, BACKGROUND_NAME),
            ThemeImageStore.draftBackgroundFile(context, dark),
        )
        copyIfExists(
            File(sourceDir, CARD_BACKGROUND_NAME),
            ThemeImageStore.draftCardBackgroundFile(context, dark),
        )
        (0 until HomeCardSticker.MAX_COUNT).forEach { index ->
            copyIfExists(
                File(sourceDir, STICKER_PREFIX + index),
                ThemeImageStore.draftStickerFile(context, index, dark),
            )
        }
    }

    private fun copyIfExists(source: File, target: File) {
        if (source.isFile) {
            target.parentFile?.mkdirs()
            source.copyTo(target, overwrite = true)
        }
    }

    private fun promoteDraft(context: Context, dark: Boolean) {
        val sourceDir = ThemeImageStore.draftVariantDir(context, dark)
        val targetDir = ThemeImageStore.variantDir(context, dark)
        if (!sourceDir.isDirectory) return
        targetDir.mkdirs()
        sourceDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name != "variant.json") {
                file.copyTo(File(targetDir, file.name), overwrite = true)
            }
        }
    }

    private fun writeVariantToPrefs(prefs: SharedPreferences, v: ThemeVariantData, dark: Boolean) {
        prefs.edit {
            v.themeMode?.let { putInt(ThemePrefKeys.COLOR_MODE, it) }
            v.miuixMonet?.let { putBoolean(ThemePrefKeys.MIUIX_MONET, it) }
            v.keyColor?.let { putInt(ThemePrefKeys.KEY_COLOR, it) }
            v.colorStyle?.let { putString(ThemePrefKeys.COLOR_STYLE, it) }
            v.colorSpec?.let { putString(ThemePrefKeys.COLOR_SPEC, it) }
            v.cardBackgroundAlphaPercent?.let {
                putInt(ThemePrefKeys.THEME_CARD_BG_ALPHA_PERCENT, it.coerceIn(10, 91))
            }
            v.customBackgroundBlurPercent?.let {
                putInt(ThemePrefKeys.THEME_BG_BLUR_PERCENT, it.coerceIn(0, 100))
            }
            v.customBackgroundScrimPercent?.let {
                putInt(ThemePrefKeys.THEME_BG_SCRIM_PERCENT, it.coerceIn(-100, 100))
            }
            v.hasBackground?.let { putBoolean(ThemePrefKeys.THEME_HAS_BACKGROUND, it) }
            v.hasCardBackground?.let { putBoolean(ThemePrefKeys.THEME_HAS_CARD_BACKGROUND, it) }
            v.hideHomeCardStatusText?.let {
                putBoolean(ThemePrefKeys.THEME_HIDE_HOME_CARD_STATUS_TEXT, it)
            }
            v.hideHomeCardDecoration?.let {
                putBoolean(ThemePrefKeys.THEME_HIDE_HOME_CARD_DECORATION, it)
            }
            v.homeCardStickerTransforms?.let { transforms ->
                putString(
                    ThemePrefKeys.THEME_STICKER_TRANSFORMS,
                    json.encodeToString(transforms),
                )
            }
            v.homeLayout?.let { putString(ThemePrefKeys.THEME_HOME_LAYOUT, it) }
            v.homeStatusAlignment?.let {
                putString(ThemePrefKeys.THEME_HOME_STATUS_ALIGNMENT, it)
            }
            v.homeStatusScale?.let { putFloat(ThemePrefKeys.THEME_HOME_STATUS_SCALE, it) }
            v.homePillScale?.let { putFloat(ThemePrefKeys.THEME_HOME_PILL_SCALE, it) }
            v.homeStatusWidthFraction?.let {
                putFloat(ThemePrefKeys.THEME_HOME_STATUS_WIDTH_FRACTION, it)
            }
            v.homeCardImageStyle?.let { putString(ThemePrefKeys.THEME_HOME_CARD_IMAGE_STYLE, it) }
            v.showHomeCardImageWithoutBackground?.let {
                putBoolean(ThemePrefKeys.THEME_SHOW_HOME_CARD_IMAGE_WITHOUT_BACKGROUND, it)
            }
            v.uiMode?.let { putString(ThemePrefKeys.UI_MODE, it) }
            v.enableFloatingBottomBar?.let {
                putBoolean(ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR, it)
            }
            v.enableFloatingBottomBarBlur?.let {
                putBoolean(ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR_BLUR, it)
            }
            v.enableNavigationBadge?.let {
                putBoolean(ThemePrefKeys.THEME_ENABLE_NAVIGATION_BADGE, it)
            }
            v.enablePredictiveBack?.let { putBoolean(ThemePrefKeys.ENABLE_PREDICTIVE_BACK, it) }
            v.enableSwipeDismiss?.let {
                putBoolean(ThemePrefKeys.THEME_ENABLE_SWIPE_DISMISS, it)
            }
            v.enableBlur?.let { putBoolean(ThemePrefKeys.ENABLE_BLUR, it) }
            v.enableProgressiveBlur?.let {
                putBoolean(ThemePrefKeys.THEME_ENABLE_PROGRESSIVE_BLUR, it)
            }
            v.pagerInterceptionMode?.let {
                putInt(ThemePrefKeys.THEME_PAGER_INTERCEPTION_MODE, it.coerceIn(0, 2))
            }
            v.pageScale?.let { putFloat(ThemePrefKeys.THEME_PAGE_SCALE, it.coerceIn(0.8f, 1.1f)) }
            v.moduleDescriptionMaxLines?.let {
                putInt(ThemePrefKeys.THEME_MODULE_DESCRIPTION_MAX_LINES, it.coerceIn(1, 5))
            }
        }
    }

    /** Effective dark mode from the saved color mode and the system setting. */
    private fun currentDarkMode(prefs: SharedPreferences): Boolean {
        val mode = prefs.getInt(ThemePrefKeys.COLOR_MODE, 0)
        return when (mode) {
            1, 4 -> false
            2, 5, 6 -> true
            else -> {
                val config = aefoxApp.resources.configuration
                (config.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}

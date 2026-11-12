package aefox.reyang.ui.theme.themepackage

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.annotation.StringRes
import aefox.reyang.R
import aefox.reyang.aefoxApp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.core.content.edit

/** Types of manager operations, used for the external state flow. */
enum class ThemePackageOp {
    IMPORT,
    APPLY,
    TRIAL_START,
    TRIAL_CANCEL,
    RESTORE_DEFAULT,
    EXPORT,
}

/**
 * Custom-text groups provided by the apply dialog (mirrors SukiSU's
 * CustomTextGroup). The group of a package key decides which child option in
 * the dialog controls it.
 */
enum class CustomTextGroup(@StringRes val titleRes: Int) {
    HOME(R.string.custom_text_group_home),
    PAGE_MAIN(R.string.custom_text_group_page_main),
    PAGE_FEATURE(R.string.custom_text_group_page_feature),
    SETTINGS(R.string.custom_text_group_settings),
}

/** Group of a custom-text key; null for keys this app does not consume. */
fun customTextGroupOf(key: String): CustomTextGroup? = when {
    key == "page.home" ||
        key == "page.superuser" ||
        key == "page.settings" -> CustomTextGroup.PAGE_MAIN

    key == "page.appearance" ||
        key == "page.custom_texts" ||
        key == "page.app_icon" ||
        key == "page.about" -> CustomTextGroup.PAGE_FEATURE

    key.startsWith("page.") -> CustomTextGroup.PAGE_MAIN
    key.startsWith("settings.") -> CustomTextGroup.SETTINGS
    key.startsWith("home.") -> CustomTextGroup.HOME
    else -> null
}

/** Groups that carry at least one non-blank text in the package. */
fun customTextGroupsIn(
    customTexts: Map<String, String>,
    customHomeTitle: String = "",
): Set<CustomTextGroup> = buildSet {
    customTexts.forEach { (key, value) ->
        if (value.isNotBlank()) customTextGroupOf(key)?.let { add(it) }
    }
    if (customHomeTitle.isNotBlank()) add(CustomTextGroup.HOME)
}

/** Keeps only the texts of the selected groups. */
fun customTextsInGroups(
    customTexts: Map<String, String>,
    groups: Set<CustomTextGroup>,
): Map<String, String> = customTexts.filter { (key, _) ->
    customTextGroupOf(key)?.let { it in groups } == true
}

/**
 * Fine-grained parts of a theme package the user can choose to apply. Unlike
 * the legacy coarse [ThemeApplySection] export groups, every field here maps
 * to one row in the apply dialog. Unchecked parts keep their current values.
 */
data class ThemeApplyOptions(
    val colors: Boolean = true,
    val customColors: Boolean = true,
    val backgroundImage: Boolean = true,
    val backgroundAppearance: Boolean = true,
    val imageEffects: Boolean = true,
    val homeCardImage: Boolean = true,
    val stickers: Boolean = true,
    val homeCardDisplay: Boolean = true,
    val appIcon: Boolean = true,
    val customTexts: Boolean = true,
    val customTextGroups: Set<CustomTextGroup> = CustomTextGroup.entries.toSet(),
    val bottomBarIcons: Boolean = true,
    val interfaceMode: Boolean = true,
    val interfaceSwitches: Boolean = true,
    val interactionBehavior: Boolean = true,
    val moduleDisplay: Boolean = true,
) {
    companion object {
        /** Applies every part the package actually carries (trial, restore). */
        val ALL: ThemeApplyOptions = ThemeApplyOptions()
    }
}

/**
 * Content availability computed from a parsed package. The apply dialog hides
 * every group and row whose data is absent, so no switch can be turned on
 * without a matching payload.
 */
data class ThemeApplyCapabilities(
    val hasBackgroundImage: Boolean,
    val hasCardImage: Boolean,
    val hasStickers: Boolean,
    val hasAppIcon: Boolean,
    val hasCustomColors: Boolean,
    val hasImageEffects: Boolean,
    val hasBottomBarIcons: Boolean,
    val availableCustomTextGroups: Set<CustomTextGroup>,
)

/** Computes the apply-dialog availability from the actual package payload. */
fun ImportedTheme.applyCapabilities(): ThemeApplyCapabilities {
    val top = data
    return ThemeApplyCapabilities(
        hasBackgroundImage = backgroundPath != null ||
            light?.backgroundPath != null ||
            dark?.backgroundPath != null ||
            (!isAdaptive && top.hasBackground == false),
        hasCardImage = cardBackgroundPath != null ||
            light?.cardBackgroundPath != null ||
            dark?.cardBackgroundPath != null ||
            (!isAdaptive && top.hasCardBackground == false),
        hasStickers = stickerPaths.isNotEmpty() ||
            light?.stickerPaths?.isNotEmpty() == true ||
            dark?.stickerPaths?.isNotEmpty() == true ||
            (!isAdaptive && top.homeCardStickerTransforms != null),
        hasAppIcon = !top.appIconId.isNullOrBlank(),
        hasCustomColors = top.appearance?.colors != null,
        hasImageEffects = top.appearance?.image != null,
        hasBottomBarIcons = top.bottomBarIcons != null || bottomBarIconPaths.isNotEmpty(),
        availableCustomTextGroups = customTextGroupsIn(
            customTexts = top.customTexts.orEmpty(),
            customHomeTitle = top.customHomeTitle.orEmpty(),
        ),
    )
}

/** Whether any option reads the adaptive light/dark variants. */
private fun usesAdaptiveVariant(options: ThemeApplyOptions): Boolean =
    options.colors ||
        options.backgroundImage ||
        options.backgroundAppearance ||
        options.homeCardImage ||
        options.stickers ||
        options.homeCardDisplay ||
        options.interfaceMode ||
        options.interfaceSwitches ||
        options.interactionBehavior ||
        options.moduleDisplay

/** Whether any option controls the background/card image source. */
private fun touchesImageSource(options: ThemeApplyOptions): Boolean =
    options.backgroundImage || options.homeCardImage || options.stickers

/**
 * Selectable sections of a theme package export. This is also the legacy
 * coarse apply grouping kept for the export dialog; applying a package uses
 * the fine-grained [ThemeApplyOptions].
 */
enum class ThemeApplySection {
    /** Color mode, Monet flavor, key color, palette style/spec and UI mode. */
    COLORS,

    /** Background/card images (incl. stickers), their appearance params and home card display. */
    IMAGES,

    /** Custom home title and custom texts. */
    TEXTS,

    /** Static appearance overlay (custom color tokens and image effects). */
    OVERLAY,

    /**
     * App list sorting options (Apps page). Kept the historical "module" name:
     * the persisted ThemePrefKeys.THEME_MODULE_SORT_* preference keys are
     * inherited from the SukiSU manager and must not be renamed, but this app
     * has no modules - the options drive the app list sort order.
     */
    MODULE_SORT,
    ;

    companion object {
        /** All sections: the default full export. */
        val ALL: Set<ThemeApplySection> = entries.toSet()
    }
}

/** Execution stage of one operation, for later ViewModel/UI progress display. */
enum class ThemePackageStage {
    PARSING,
    BACKUP,
    WRITE_SETTINGS,
    PROMOTE_IMAGES,
    CLEANUP,
    ROLLBACK,
    RESTORE,
}

/** Observable manager state: idle / working (stage) / done / failed (reason + rolled back). */
sealed class ThemePackageState {
    data object Idle : ThemePackageState()
    data class Working(val op: ThemePackageOp, val stage: ThemePackageStage) : ThemePackageState()
    data class Done(val op: ThemePackageOp) : ThemePackageState()
    data class Failed(
        val op: ThemePackageOp,
        val reason: String,
        val rolledBack: Boolean,
    ) : ThemePackageState()
}

/** Wrapper thrown when apply fails and a rollback has run; carries the rollback result to the state flow. */
private class ThemeApplyException(val rolledBack: Boolean, cause: Throwable) :
    RuntimeException(cause.message, cause)

/**
 * All prefs key conventions for theme package appearance settings.
 *
 * Colors and global switches reuse existing keys; the rest of the appearance
 * fields uniformly use the `theme_` prefix + snake_case.
 */
internal object ThemePrefKeys {
    // ---- Existing keys: colors / UI mode / global switches ----
    const val UI_MODE = "ui_mode"
    const val COLOR_MODE = "color_mode"
    const val MIUIX_MONET = "miuix_monet"
    const val PREF_COLOR = "pref_color"
    const val COLOR_STYLE = "color_style"
    const val COLOR_SPEC = "color_spec"
    const val ENABLE_BLUR = "enable_blur"
    const val ENABLE_FLOATING_BOTTOM_BAR = "enable_floating_bottom_bar"
    const val ENABLE_FLOATING_BOTTOM_BAR_BLUR = "enable_floating_bottom_bar_blur"
    const val ENABLE_PREDICTIVE_BACK = "enable_predictive_back"

    // ---- theme_-prefixed keys (camelCase -> snake_case) ----
    const val THEME_HAS_BACKGROUND = "theme_has_background"
    const val THEME_BACKGROUND_PATH = "theme_background_path"
    const val THEME_BG_BLUR_PERCENT = "theme_bg_blur_percent"
    const val THEME_BG_SCRIM_PERCENT = "theme_bg_scrim_percent"
    const val THEME_HAS_CARD_BACKGROUND = "theme_has_card_background"
    const val THEME_CARD_BACKGROUND_PATH = "theme_card_background_path"
    const val THEME_CARD_BG_ALPHA_PERCENT = "theme_card_bg_alpha_percent"
    const val THEME_ADAPTIVE = "theme_adaptive"
    const val THEME_LIGHT_DIR = "theme_light_dir"
    const val THEME_DARK_DIR = "theme_dark_dir"
    const val THEME_STICKER_TRANSFORMS = "theme_sticker_transforms"
    const val THEME_BOTTOM_BAR_ICONS = "theme_bottom_bar_icons"
    const val THEME_HOME_LAYOUT = "theme_home_layout"
    const val THEME_HOME_STATUS_ALIGNMENT = "theme_home_status_alignment"
    const val THEME_HOME_STATUS_SCALE = "theme_home_status_scale"
    const val THEME_HOME_PILL_SCALE = "theme_home_pill_scale"
    const val THEME_HOME_STATUS_WIDTH_FRACTION = "theme_home_status_width_fraction"
    const val THEME_HIDE_HOME_CARD_STATUS_TEXT = "theme_hide_home_card_status_text"
    const val THEME_HIDE_HOME_CARD_DECORATION = "theme_hide_home_card_decoration"
    const val THEME_HOME_CARD_IMAGE_STYLE = "theme_home_card_image_style"
    const val THEME_SHOW_HOME_CARD_IMAGE_WITHOUT_BACKGROUND =
        "theme_show_home_card_image_without_background"
    const val THEME_CUSTOM_HOME_TITLE = "theme_custom_home_title"
    const val THEME_CUSTOM_TEXTS = "theme_custom_texts"
    const val THEME_MODULE_SORT_ENABLED_FIRST = "theme_module_sort_enabled_first"
    const val THEME_MODULE_SORT_ACTION_FIRST = "theme_module_sort_action_first"
    const val THEME_MODULE_SORT_WEB_UI_FIRST = "theme_module_sort_web_ui_first"
    const val THEME_MODULE_SORT_MODULE_FIRST = "theme_module_sort_module_first"
    const val THEME_MODULE_SORT_METAMODULE_FIRST = "theme_module_sort_metamodule_first"
    const val THEME_MODULE_SORT_NAME_ORDER = "theme_module_sort_name_order"
    const val THEME_MODULE_SORT_MANUAL = "theme_module_sort_manual"
    const val THEME_ENABLE_NAVIGATION_BADGE = "theme_enable_navigation_badge"
    const val THEME_ENABLE_PREDICTIVE_BACK = "theme_enable_predictive_back"
    const val THEME_ENABLE_SWIPE_DISMISS = "theme_enable_swipe_dismiss"
    const val THEME_PAGER_INTERCEPTION_MODE = "theme_pager_interception_mode"
    const val THEME_PAGE_SCALE = "theme_page_scale"
    const val THEME_MODULE_DESCRIPTION_MAX_LINES = "theme_module_description_max_lines"
    const val THEME_ACTIVE_ID = "theme_active_id"
    const val THEME_ACTIVE_NAME = "theme_active_name"

    // ---- Required keys added by the same convention outside the task list (see migration report) ----
    /** Appearance overlay JSON (AppearanceOverlayData). */
    const val THEME_APPEARANCE = "theme_appearance"

    /** Progressive blur switch (AeFox has no corresponding existing key yet). */
    const val THEME_ENABLE_PROGRESSIVE_BLUR = "theme_enable_progressive_blur"

    // ---- Appearance fields added by the theme-gap migration (keys follow the theme_ convention) ----

    /** Selected launcher icon id; applied by the app-icon screen and by package apply. */
    const val THEME_APP_ICON_ID = "theme_app_icon_id"

    /** Whether the selected launcher icon uses its dynamic-color variant. */
    const val THEME_APP_ICON_DYNAMIC_COLOR = "theme_app_icon_dynamic_color"

    /** Use the system wallpaper (captured once) as the global background. */
    const val THEME_USE_SYSTEM_WALLPAPER = "theme_use_system_wallpaper"

    /** Derive the key color from the active background image (exclusive with a fixed key color). */
    const val THEME_EXTRACT_COLOR_FROM_BACKGROUND = "theme_extract_color_from_background"

    /** Timestamp of the last background write; the fixed image path needs it for cache busting. */
    const val THEME_BACKGROUND_UPDATED_AT = "theme_background_updated_at"

    /** Separate home-card image (the immersive banner), distinct from the card background. */
    const val THEME_HOME_CARD_IMAGE_PATH = "theme_home_card_image_path"

    /** Adaptive-theme builder draft JSON (light/dark variants being edited). */
    const val THEME_ADAPTIVE_DRAFT = "theme_adaptive_draft"

    /** Applied adaptive variant mode: -1 none, 0 light, 1 dark. */
    const val THEME_ADAPTIVE_APPLIED_MODE = "theme_adaptive_applied_mode"

    /** Manual module order as a JSON array of module ids. */
    const val THEME_MODULE_MANUAL_ORDER = "theme_module_manual_order"

    /** All theme_ keys to clear wholesale when restoreDefault has no backup path. */
    val ALL_THEME_KEYS = listOf(
        THEME_HAS_BACKGROUND,
        THEME_BACKGROUND_PATH,
        THEME_BG_BLUR_PERCENT,
        THEME_BG_SCRIM_PERCENT,
        THEME_HAS_CARD_BACKGROUND,
        THEME_CARD_BACKGROUND_PATH,
        THEME_CARD_BG_ALPHA_PERCENT,
        THEME_ADAPTIVE,
        THEME_LIGHT_DIR,
        THEME_DARK_DIR,
        THEME_STICKER_TRANSFORMS,
        THEME_BOTTOM_BAR_ICONS,
        THEME_HOME_LAYOUT,
        THEME_HOME_STATUS_ALIGNMENT,
        THEME_HOME_STATUS_SCALE,
        THEME_HOME_PILL_SCALE,
        THEME_HOME_STATUS_WIDTH_FRACTION,
        THEME_HIDE_HOME_CARD_STATUS_TEXT,
        THEME_HIDE_HOME_CARD_DECORATION,
        THEME_HOME_CARD_IMAGE_STYLE,
        THEME_SHOW_HOME_CARD_IMAGE_WITHOUT_BACKGROUND,
        THEME_CUSTOM_HOME_TITLE,
        THEME_CUSTOM_TEXTS,
        THEME_MODULE_SORT_ENABLED_FIRST,
        THEME_MODULE_SORT_ACTION_FIRST,
        THEME_MODULE_SORT_WEB_UI_FIRST,
        THEME_MODULE_SORT_MODULE_FIRST,
        THEME_MODULE_SORT_METAMODULE_FIRST,
        THEME_MODULE_SORT_NAME_ORDER,
        THEME_MODULE_SORT_MANUAL,
        THEME_ENABLE_NAVIGATION_BADGE,
        THEME_ENABLE_PREDICTIVE_BACK,
        THEME_ENABLE_SWIPE_DISMISS,
        THEME_PAGER_INTERCEPTION_MODE,
        THEME_PAGE_SCALE,
        THEME_MODULE_DESCRIPTION_MAX_LINES,
        THEME_ACTIVE_ID,
        THEME_ACTIVE_NAME,
        THEME_APPEARANCE,
        THEME_ENABLE_PROGRESSIVE_BLUR,
        THEME_APP_ICON_ID,
        THEME_APP_ICON_DYNAMIC_COLOR,
        THEME_USE_SYSTEM_WALLPAPER,
        THEME_EXTRACT_COLOR_FROM_BACKGROUND,
        THEME_BACKGROUND_UPDATED_AT,
        THEME_HOME_CARD_IMAGE_PATH,
        THEME_ADAPTIVE_DRAFT,
        THEME_ADAPTIVE_APPLIED_MODE,
        THEME_MODULE_MANUAL_ORDER,
    )
}

/**
 * Layout and read/write of theme image/backup directories under filesDir.
 *
 * Structure:
 * ```
 * filesDir/theme/
 *   background | card_background | card_sticker_0..4 | bottom_bar_icon_<slot>   (plain package)
 *   light/{background, card_background, card_sticker_0..4, variant.json}        (adaptive light)
 *   dark/{...}                                                                  (adaptive dark)
 *   backup/{backup.sukisu, original.sukisu}
 * filesDir/theme_trial/backup.sukisu
 * ```
 */
@OptIn(ExperimentalSerializationApi::class)
internal object ThemeImageStore {
    private const val DIR_NAME = "theme"
    private const val LIGHT_DIR_NAME = "light"
    private const val DARK_DIR_NAME = "dark"
    private const val BACKUP_DIR_NAME = "backup"
    private const val BACKUP_FILE_NAME = "backup.sukisu"
    private const val ORIGINAL_FILE_NAME = "original.sukisu"
    private const val VARIANT_JSON_NAME = "variant.json"
    private const val BACKGROUND_NAME = "background"
    private const val CARD_BACKGROUND_NAME = "card_background"
    private const val HOME_CARD_IMAGE_NAME = "home_card_image"
    private const val SYSTEM_WALLPAPER_NAME = "system_wallpaper"
    private const val CARD_STICKER_PREFIX = "card_sticker_"
    private const val BOTTOM_BAR_ICON_PREFIX = "bottom_bar_icon_"
    private const val TRIAL_DIR_NAME = "theme_trial"
    private const val TRIAL_BACKUP_NAME = "backup.sukisu"
    private const val DRAFT_DIR_NAME = "draft"

    private val variantJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun themeDir(context: Context): File = File(context.filesDir, DIR_NAME)

    fun variantDir(context: Context, dark: Boolean): File =
        File(themeDir(context), if (dark) DARK_DIR_NAME else LIGHT_DIR_NAME)

    /** dark == null means the plain package's top-level location; true/false means the adaptive variant directory. */
    fun backgroundFile(context: Context, dark: Boolean?): File =
        baseFile(context, dark, BACKGROUND_NAME)

    fun cardBackgroundFile(context: Context, dark: Boolean?): File =
        baseFile(context, dark, CARD_BACKGROUND_NAME)

    /** User-picked immersive banner image, independent from the package card background. */
    fun homeCardImageFile(context: Context, dark: Boolean?): File =
        baseFile(context, dark, HOME_CARD_IMAGE_NAME)

    /** Captured system wallpaper (plain top-level file, shared by both modes). */
    fun systemWallpaperFile(context: Context): File =
        File(themeDir(context), SYSTEM_WALLPAPER_NAME)

    fun stickerFile(context: Context, index: Int, dark: Boolean?): File =
        baseFile(context, dark, CARD_STICKER_PREFIX + index)

    fun bottomBarIconFile(context: Context, slotKey: String): File =
        File(themeDir(context), BOTTOM_BAR_ICON_PREFIX + slotKey)

    fun backupDir(context: Context): File = File(themeDir(context), BACKUP_DIR_NAME)

    fun backupFile(context: Context): File = File(backupDir(context), BACKUP_FILE_NAME)

    fun originalFile(context: Context): File = File(backupDir(context), ORIGINAL_FILE_NAME)

    fun trialDir(context: Context): File = File(context.filesDir, TRIAL_DIR_NAME)

    fun trialBackupFile(context: Context): File = File(trialDir(context), TRIAL_BACKUP_NAME)

    /** Adaptive builder draft directory for one mode; images are promoted to the variant dirs on apply. */
    fun draftVariantDir(context: Context, dark: Boolean): File =
        File(File(themeDir(context), DRAFT_DIR_NAME), if (dark) DARK_DIR_NAME else LIGHT_DIR_NAME)

    fun draftBackgroundFile(context: Context, dark: Boolean): File =
        File(draftVariantDir(context, dark), BACKGROUND_NAME)

    fun draftCardBackgroundFile(context: Context, dark: Boolean): File =
        File(draftVariantDir(context, dark), CARD_BACKGROUND_NAME)

    fun draftStickerFile(context: Context, index: Int, dark: Boolean): File =
        File(draftVariantDir(context, dark), CARD_STICKER_PREFIX + index)

    fun draftVariantJsonFile(context: Context, dark: Boolean): File =
        File(draftVariantDir(context, dark), VARIANT_JSON_NAME)

    fun clearDraft(context: Context) {
        File(themeDir(context), DRAFT_DIR_NAME).deleteRecursively()
    }

    private fun baseFile(context: Context, dark: Boolean?, name: String): File =
        if (dark == null) {
            File(themeDir(context), name)
        } else {
            File(variantDir(context, dark), name)
        }

    /** Promote a temporary file to a permanent image: prefer rename; fall back to stream copy across volumes. */
    fun promote(temp: File, target: File): String {
        target.parentFile?.mkdirs()
        if (target.isFile && !target.delete()) {
            error("Failed to replace theme image: ${target.name}")
        }
        if (!temp.renameTo(target)) {
            temp.inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return target.absolutePath
    }

    fun readVariantData(context: Context, dark: Boolean): ThemeVariantData? {
        val file = File(variantDir(context, dark), VARIANT_JSON_NAME)
        if (!file.isFile) return null
        return runCatching {
            variantJson.decodeFromString(ThemeVariantData.serializer(), file.readText())
        }.getOrNull()
    }

    fun writeVariantData(context: Context, dark: Boolean, data: ThemeVariantData) {
        val dir = variantDir(context, dark)
        dir.mkdirs()
        File(dir, VARIANT_JSON_NAME).writeText(
            variantJson.encodeToString(ThemeVariantData.serializer(), data),
        )
    }

    /** Write variant data into an explicit directory (adaptive builder draft). */
    fun writeVariantDataAt(dir: File, data: ThemeVariantData) {
        dir.mkdirs()
        File(dir, VARIANT_JSON_NAME).writeText(
            variantJson.encodeToString(ThemeVariantData.serializer(), data),
        )
    }

    /** Clear top-level plain-package images; the backup directory and other contents are untouched. */
    fun clearTopLevelImages(context: Context) {
        themeDir(context)
            .listFiles { file ->
                file.isFile && (
                    file.name == BACKGROUND_NAME ||
                        file.name == CARD_BACKGROUND_NAME ||
                        file.name == HOME_CARD_IMAGE_NAME ||
                        file.name.startsWith(CARD_STICKER_PREFIX)
                    )
            }?.forEach { it.delete() }
    }

    /** Clear one mode variant directory (images + variant JSON). */
    fun clearVariantImages(context: Context, dark: Boolean) {
        variantDir(context, dark).deleteRecursively()
    }

    /** Clear all custom bottom bar icons. */
    fun clearBottomBarIcons(context: Context) {
        themeDir(context)
            .listFiles { file -> file.isFile && file.name.startsWith(BOTTOM_BAR_ICON_PREFIX) }
            ?.forEach { it.delete() }
    }
}

/**
 * Trial state persistence. The prefs name and key conventions align with
 * SukiSU's ThemeTrialManager (AeFox is single-task, so the taskId dimension is
 * dropped and restore timing is judged by process generation only).
 */
internal object ThemeTrialStore {
    private const val PREFS_NAME = "theme_trial"
    private const val PREF_ACTIVE = "active"
    private const val PREF_BACKUP = "backup"
    private const val PREF_PROCESS = "process"
    private const val PREF_ACTIVE_ID = "active_id"
    private const val PREF_ACTIVE_NAME = "active_name"

    /** Process generation marker: its value changes after a process restart, used to trigger auto-restore. */
    private val processToken: String = UUID.randomUUID().toString()

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isActive(context: Context): Boolean = prefs(context).getBoolean(PREF_ACTIVE, false)

    /** Whether a trial is pending restore (the active marker exists and the process generation has changed). */
    fun needsRestore(context: Context): Boolean {
        val trialPrefs = prefs(context)
        if (!trialPrefs.getBoolean(PREF_ACTIVE, false)) return false
        return trialPrefs.getString(PREF_PROCESS, null) != processToken
    }

    fun activeBackup(context: Context): File? {
        val prefs = prefs(context)
        if (!prefs.getBoolean(PREF_ACTIVE, false)) return null
        val file = prefs.getString(PREF_BACKUP, null)?.let(::File)
        if (file == null || !file.isFile) {
            clear(context)
            return null
        }
        return file
    }

    fun markActive(context: Context, backup: File, activeId: String, activeName: String) {
        prefs(context).edit {
            putBoolean(PREF_ACTIVE, true)
            putString(PREF_BACKUP, backup.absolutePath)
            putString(PREF_PROCESS, processToken)
            putString(PREF_ACTIVE_ID, activeId)
            putString(PREF_ACTIVE_NAME, activeName)
        }
    }

    /** Write the theme active markers from before the trial back into settings. */
    fun restoreMarkers(context: Context) {
        val trialPrefs = prefs(context)
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit {
            putString(ThemePrefKeys.THEME_ACTIVE_ID, trialPrefs.getString(PREF_ACTIVE_ID, ""))
            putString(ThemePrefKeys.THEME_ACTIVE_NAME, trialPrefs.getString(PREF_ACTIVE_NAME, ""))
        }
    }

    fun clear(context: Context) {
        prefs(context).edit { clear() }
        ThemeImageStore.trialDir(context).deleteRecursively()
    }
}

/**
 * Import/apply/trial/backup-restore/export manager for .sukisu theme packages
 * (pure data and file layer, no ViewModel dependency).
 *
 * - Apply: export the "current appearance" as a rollback package first, then
 *   write the persistent backup, finally write settings and promote images;
 *   rollback automatically on failure.
 * - Trial: export the whole current appearance to
 *   `filesDir/theme_trial/backup.sukisu`, auto-restore on failure, undoable.
 * - State is provided via [state] for later ViewModel/UI consumption.
 */
@OptIn(ExperimentalSerializationApi::class)
class ThemePackageManager(private val context: Context = aefoxApp) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val _state = MutableStateFlow<ThemePackageState>(ThemePackageState.Idle)
    val state: StateFlow<ThemePackageState> = _state.asStateFlow()

    // ------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------

    /**
     * Parse a .sukisu package and unpack temporary images into cacheDir; does
     * not apply on disk. After the caller applies, the manager cleans up the
     * temporary files; call [discard] when giving up an import.
     */
    suspend fun import(file: File): Result<ImportedTheme> =
        runOp(ThemePackageOp.IMPORT, ThemePackageStage.PARSING) {
            ThemePackageStorage.importFromFile(context, file).getOrThrow()
        }

    /** Discard an import result that has not been applied, deleting all temporary files. */
    fun discard(imported: ImportedTheme) {
        imported.temporaryFiles().forEach { path -> runCatching { File(path).delete() } }
    }

    // ------------------------------------------------------------------
    // Apply (permanent)
    // ------------------------------------------------------------------

    /**
     * Permanently apply an imported package: back up the current appearance ->
     * write the selected settings and promote the selected images -> clean up
     * temporary files. Any failure restores the pre-apply state from the
     * rollback package and records the rollback result in the state flow.
     *
     * [options] limits which fine-grained parts of the package are applied;
     * unchecked or absent parts keep their current values. Restore paths use
     * [ThemeApplyOptions.ALL] so backups stay complete.
     */
    suspend fun apply(
        imported: ImportedTheme,
        name: String = "",
        options: ThemeApplyOptions = ThemeApplyOptions.ALL,
    ): Result<Unit> =
        runOp(ThemePackageOp.APPLY, ThemePackageStage.BACKUP) {
            // Transient rollback package: export the full current appearance to cache, consistent with SukiSU.
            val rollback = File(context.cacheDir, ROLLBACK_FILE_NAME)
            exportSnapshot(rollback)
            writePersistentBackups(rollback)
            try {
                applyInternal(imported, markActive = true, name = name, options = options)
            } catch (error: Throwable) {
                setStage(ThemePackageStage.ROLLBACK)
                val rolledBack = rollbackRestore(rollback)
                throw ThemeApplyException(rolledBack, error)
            } finally {
                runCatching { rollback.delete() }
            }
            // A permanent apply supersedes an in-progress trial (aligns with SukiSU confirmImportTheme).
            ThemeTrialStore.clear(context)
        }

    /**
     * Persistent backups: backup.sukisu is refreshed to the "replaced
     * appearance" before every apply; original.sukisu is only written on the
     * first apply and saves the "no theme" original state (including original
     * color field values).
     */
    private fun writePersistentBackups(rollback: File) {
        ThemeImageStore.backupDir(context).mkdirs()
        rollback.copyTo(ThemeImageStore.backupFile(context), overwrite = true)
        val original = ThemeImageStore.originalFile(context)
        if (!original.isFile) {
            rollback.copyTo(original, overwrite = true)
        }
    }

    /** Restore the pre-apply appearance from a rollback package; returns whether it succeeded. Does not rewrite active markers. */
    private suspend fun rollbackRestore(rollback: File): Boolean {
        if (!rollback.isFile) return false
        return runCatching {
            val imported = ThemePackageStorage.importFromFile(context, rollback).getOrThrow()
            applyInternal(imported, markActive = false, name = "", options = ThemeApplyOptions.ALL)
        }.isSuccess
    }

    // ------------------------------------------------------------------
    // Trial
    // ------------------------------------------------------------------

    /**
     * Start a trial: on the first trial, export the whole current appearance as
     * the trial backup, then apply the trial package; auto-restore the backup on
     * apply failure. The trial package file is deleted regardless of outcome
     * (aligns with SukiSU beginThemeTrial).
     */
    suspend fun startTrial(packageFile: File): Result<Unit> =
        runOp(ThemePackageOp.TRIAL_START, ThemePackageStage.PARSING) {
            withContext(NonCancellable) {
                if (ThemeTrialStore.activeBackup(context) == null) {
                    val snapshot = File(context.cacheDir, TRIAL_SNAPSHOT_FILE_NAME)
                    exportSnapshot(snapshot)
                    val target = ThemeImageStore.trialBackupFile(context)
                    target.parentFile?.mkdirs()
                    snapshot.copyTo(target, overwrite = true)
                    snapshot.delete()
                    ThemeTrialStore.markActive(
                        context,
                        target,
                        readActiveId(),
                        readActiveName(),
                    )
                }
                val applied = runCatching {
                    val imported = ThemePackageStorage.importFromFile(context, packageFile)
                        .getOrThrow()
                    applyInternal(
                        imported,
                        markActive = true,
                        name = trialDisplayName(packageFile),
                        options = ThemeApplyOptions.ALL,
                    )
                }
                if (applied.isFailure) {
                    // Auto-restore to the pre-trial state on apply failure (aligns with SukiSU beginThemeTrial).
                    setStage(ThemePackageStage.ROLLBACK)
                    restoreTrialBackupInternal()
                }
                // The trial package is one-shot: delete it regardless of outcome (aligns with SukiSU).
                runCatching { packageFile.delete() }
                applied.getOrThrow()
            }
        }

    /** Cancel a trial: parse the trial backup and re-apply it, clearing the trial state on success (aligns with SukiSU restoreTrialBackup). */
    suspend fun cancelTrial(): Result<Unit> =
        runOp(ThemePackageOp.TRIAL_CANCEL, ThemePackageStage.RESTORE) {
            if (!restoreTrialBackupInternal()) {
                error("No active trial backup to restore")
            }
        }

    /** Whether an active trial currently exists. */
    fun trialActive(): Boolean = ThemeTrialStore.isActive(context)

    /**
     * Whether a trial is pending restore (the trial spanned a process). The host
     * Activity should query this at startup before calling
     * [restoreTrialIfNeeded] (aligns with SukiSU MainActivity startup restore).
     */
    fun trialNeedsRestore(): Boolean = ThemeTrialStore.needsRestore(context)

    /** Restore the pre-trial appearance after a process generation change; clears the trial state on success. */
    suspend fun restoreTrialIfNeeded() {
        if (!trialNeedsRestore()) return
        withContext(Dispatchers.IO) {
            restoreTrialBackupInternal()
        }
    }

    /** Restore the trial backup (if any). On success, restores the pre-trial active markers and clears the trial state. */
    private suspend fun restoreTrialBackupInternal(): Boolean {
        val backup = ThemeTrialStore.activeBackup(context) ?: return false
        val imported = ThemePackageStorage.importFromFile(context, backup).getOrNull()
            ?: return false
        val applied = runCatching {
            applyInternal(imported, markActive = false, name = "", options = ThemeApplyOptions.ALL)
        }.isSuccess
        if (applied) {
            ThemeTrialStore.restoreMarkers(context)
            ThemeTrialStore.clear(context)
        }
        return applied
    }

    private fun trialDisplayName(packageFile: File): String =
        packageFile.nameWithoutExtension.ifBlank {
            "theme-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        }

    // ------------------------------------------------------------------
    // Restore default
    // ------------------------------------------------------------------

    /**
     * Restore the default appearance: prefer original.sukisu (the no-theme
     * original state), then backup.sukisu; if neither exists, clear all
     * theme_* keys and theme image files, leaving colors and existing switches
     * untouched.
     */
    suspend fun restoreDefault(): Result<Unit> =
        runOp(ThemePackageOp.RESTORE_DEFAULT, ThemePackageStage.RESTORE) {
            val original = ThemeImageStore.originalFile(context)
            val backup = ThemeImageStore.backupFile(context)
            val source = when {
                original.isFile -> original
                backup.isFile -> backup
                else -> null
            }
            if (source == null) {
                clearThemeState()
            } else {
                val imported = ThemePackageStorage.importFromFile(context, source).getOrThrow()
                applyInternal(imported, markActive = false, name = "", options = ThemeApplyOptions.ALL)
                clearActiveMarkers()
            }
            ThemeTrialStore.clear(context)
        }

    /** No-backup path: clear all theme_* keys and theme image files, keeping colors and existing global switches. */
    private fun clearThemeState() {
        prefs.edit { ThemePrefKeys.ALL_THEME_KEYS.forEach { remove(it) } }
        ThemeImageStore.clearTopLevelImages(context)
        ThemeImageStore.clearVariantImages(context, dark = false)
        ThemeImageStore.clearVariantImages(context, dark = true)
        ThemeImageStore.clearBottomBarIcons(context)
    }

    private fun clearActiveMarkers() {
        prefs.edit {
            remove(ThemePrefKeys.THEME_ACTIVE_ID)
            remove(ThemePrefKeys.THEME_ACTIVE_NAME)
        }
    }

    // ------------------------------------------------------------------
    // Export
    // ------------------------------------------------------------------

    /**
     * Export the current appearance as a v5 package (including images). When
     * [adaptive] is true, adaptive variant data is required to exist. [sections]
     * limits which appearance groups are written into the package.
     */
    suspend fun exportCurrentAppearance(
        target: File,
        adaptive: Boolean,
        sections: Set<ThemeApplySection> = ThemeApplySection.ALL,
    ): Result<Unit> =
        runOp(ThemePackageOp.EXPORT, ThemePackageStage.PARSING) {
            exportSnapshot(target, adaptive, sections)
        }

    /** Export the current appearance; adaptive defaults to the current state (used for backup/rollback snapshots). */
    private suspend fun exportSnapshot(
        target: File,
        adaptive: Boolean = currentAdaptiveState(),
        sections: Set<ThemeApplySection> = ThemeApplySection.ALL,
    ) {
        val payload = buildCurrentPayload(adaptive, sections)
        ThemePackageStorage.exportToFile(
            file = target,
            data = payload.data,
            backgroundFile = payload.backgroundFile,
            cardBackgroundFile = payload.cardBackgroundFile,
            cardStickerFiles = payload.stickerFiles,
            adaptive = payload.adaptive,
            bottomBarIconFiles = payload.bottomBarIconFiles,
        ).getOrThrow()
    }

    /** Whether the current state is a usable adaptive state (marker present and both variant data sets complete). */
    private fun currentAdaptiveState(): Boolean =
        prefs.getBoolean(ThemePrefKeys.THEME_ADAPTIVE, false) &&
            ThemeImageStore.readVariantData(context, dark = false) != null &&
            ThemeImageStore.readVariantData(context, dark = true) != null

    // ------------------------------------------------------------------
    // Apply internals
    // ------------------------------------------------------------------

    /**
     * Apply a parsed package: promote the selected images -> write the selected
     * settings -> clean up temporary files. Settings are written only after all
     * selected images are successfully promoted, avoiding "settings pointing at
     * nonexistent images". Parts the package does not carry are left untouched.
     */
    private suspend fun applyInternal(
        imported: ImportedTheme,
        markActive: Boolean,
        name: String,
        options: ThemeApplyOptions,
    ) {
        setStage(ThemePackageStage.PROMOTE_IMAGES)
        val promoted = promoteImages(imported, options)
        setStage(ThemePackageStage.WRITE_SETTINGS)
        if (imported.isAdaptive) {
            writeAdaptiveSettings(imported, promoted, options)
        } else {
            writePlainSettings(imported, promoted, options)
        }
        if (markActive) {
            writeActiveMarkers(imported.data, name)
        }
        setStage(ThemePackageStage.CLEANUP)
        imported.temporaryFiles().forEach { path -> runCatching { File(path).delete() } }
    }

    /** Plain package: all appearance fields come from the package top level. */
    private fun writePlainSettings(
        imported: ImportedTheme,
        promoted: PromotedImages,
        options: ThemeApplyOptions,
    ) {
        val projection = projectionOf(imported.data)
        writeColorFields(projection, adaptive = false, options = options)
        writeAppearanceFields(projection, dark = null, options = options)
        writeChromeFields(projection, options)
        writeGlobalFields(imported.data, promoted, options)
        if (touchesImageSource(options)) {
            prefs.edit {
                putBoolean(ThemePrefKeys.THEME_ADAPTIVE, false)
                putString(ThemePrefKeys.THEME_LIGHT_DIR, "")
                putString(ThemePrefKeys.THEME_DARK_DIR, "")
            }
        }
    }

    /** Adaptive package: color/appearance/chrome settings take the variant for the current system mode; the global group takes the top level. */
    private fun writeAdaptiveSettings(
        imported: ImportedTheme,
        promoted: PromotedImages,
        options: ThemeApplyOptions,
    ) {
        val dark = isSystemDarkMode()
        val variant = if (dark) imported.dark!! else imported.light!!
        val projection = projectionOf(variant.data)
        writeColorFields(projection, adaptive = true, options = options)
        writeAppearanceFields(projection, dark, options = options)
        writeChromeFields(projection, options)
        // Adaptive packages keep the app-sorting group at the top level (the
        // variant data carries no module sort fields).
        if (options.moduleDisplay) {
            writeModuleSortFields(imported.data)
        }
        writeGlobalFields(imported.data, promoted, options)
        // Only toggle the adaptive image source when an image-backed option was
        // selected; applying colors/texts alone keeps the current image layout.
        if (touchesImageSource(options)) {
            prefs.edit {
                putBoolean(ThemePrefKeys.THEME_ADAPTIVE, true)
                putString(ThemePrefKeys.THEME_LIGHT_DIR, promoted.lightDir)
                putString(ThemePrefKeys.THEME_DARK_DIR, promoted.darkDir)
            }
        }
    }

    /**
     * Appearance field projection: plain packages take the top-level
     * [ThemePackageData], adaptive packages take the current variant
     * [ThemeVariantData] (aligns with SukiSU: top-level appearance fields are
     * masked when adaptive).
     */
    private class AppearanceProjection(
        val themeMode: Int?,
        val miuixMonet: Boolean?,
        val keyColor: Int?,
        val colorStyle: String?,
        val colorSpec: String?,
        val cardBackgroundAlphaPercent: Int?,
        val customBackgroundBlurPercent: Int?,
        val customBackgroundScrimPercent: Int?,
        val hasBackground: Boolean?,
        val hasCardBackground: Boolean?,
        val hideHomeCardStatusText: Boolean?,
        val hideHomeCardDecoration: Boolean?,
        val homeCardStickerTransforms: List<HomeCardStickerTransform>?,
        val homeLayout: String?,
        val homeStatusAlignment: String?,
        val homeStatusScale: Float?,
        val homePillScale: Float?,
        val homeStatusWidthFraction: Float?,
        val homeCardImageStyle: String?,
        val showHomeCardImageWithoutBackground: Boolean?,
        val uiMode: String?,
        val enableBlur: Boolean?,
        val enableProgressiveBlur: Boolean?,
        val enableFloatingBottomBar: Boolean?,
        val enableFloatingBottomBarBlur: Boolean?,
        val enableNavigationBadge: Boolean?,
        val enablePredictiveBack: Boolean?,
        val enableSwipeDismiss: Boolean?,
        val pagerInterceptionMode: Int?,
        val pageScale: Float?,
        val moduleDescriptionMaxLines: Int?,
        val moduleSortEnabledFirst: Boolean?,
        val moduleSortActionFirst: Boolean?,
        val moduleSortWebUiFirst: Boolean?,
        val moduleSortModuleFirst: Boolean?,
        val moduleSortMetamoduleFirst: Boolean?,
        val moduleSortNameOrder: String?,
        val moduleSortManual: Boolean?,
    )

    private fun projectionOf(data: ThemePackageData) = AppearanceProjection(
        themeMode = data.themeMode,
        miuixMonet = data.miuixMonet,
        keyColor = data.keyColor,
        colorStyle = data.colorStyle,
        colorSpec = data.colorSpec,
        cardBackgroundAlphaPercent = data.cardBackgroundAlphaPercent,
        customBackgroundBlurPercent = data.customBackgroundBlurPercent,
        customBackgroundScrimPercent = data.customBackgroundScrimPercent,
        hasBackground = data.hasBackground,
        hasCardBackground = data.hasCardBackground,
        hideHomeCardStatusText = data.hideHomeCardStatusText,
        hideHomeCardDecoration = data.hideHomeCardDecoration,
        homeCardStickerTransforms = data.homeCardStickerTransforms,
        homeLayout = data.homeLayout,
        homeStatusAlignment = data.homeStatusAlignment,
        homeStatusScale = data.homeStatusScale,
        homePillScale = data.homePillScale,
        homeStatusWidthFraction = data.homeStatusWidthFraction,
        homeCardImageStyle = data.homeCardImageStyle,
        showHomeCardImageWithoutBackground = data.showHomeCardImageWithoutBackground,
        uiMode = data.uiMode,
        enableBlur = data.enableBlur,
        enableProgressiveBlur = data.enableProgressiveBlur,
        enableFloatingBottomBar = data.enableFloatingBottomBar,
        enableFloatingBottomBarBlur = data.enableFloatingBottomBarBlur,
        enableNavigationBadge = data.enableNavigationBadge,
        enablePredictiveBack = data.enablePredictiveBack,
        enableSwipeDismiss = data.enableSwipeDismiss,
        pagerInterceptionMode = data.pagerInterceptionMode,
        pageScale = data.pageScale,
        moduleDescriptionMaxLines = data.moduleDescriptionMaxLines,
        moduleSortEnabledFirst = data.moduleSortEnabledFirst,
        moduleSortActionFirst = data.moduleSortActionFirst,
        moduleSortWebUiFirst = data.moduleSortWebUiFirst,
        moduleSortModuleFirst = data.moduleSortModuleFirst,
        moduleSortMetamoduleFirst = data.moduleSortMetamoduleFirst,
        moduleSortNameOrder = data.moduleSortNameOrder,
        moduleSortManual = data.moduleSortManual,
    )

    private fun projectionOf(data: ThemeVariantData) = AppearanceProjection(
        themeMode = data.themeMode,
        miuixMonet = data.miuixMonet,
        keyColor = data.keyColor,
        colorStyle = data.colorStyle,
        colorSpec = data.colorSpec,
        cardBackgroundAlphaPercent = data.cardBackgroundAlphaPercent,
        customBackgroundBlurPercent = data.customBackgroundBlurPercent,
        customBackgroundScrimPercent = data.customBackgroundScrimPercent,
        hasBackground = data.hasBackground,
        hasCardBackground = data.hasCardBackground,
        hideHomeCardStatusText = data.hideHomeCardStatusText,
        hideHomeCardDecoration = data.hideHomeCardDecoration,
        homeCardStickerTransforms = data.homeCardStickerTransforms,
        homeLayout = data.homeLayout,
        homeStatusAlignment = data.homeStatusAlignment,
        homeStatusScale = data.homeStatusScale,
        homePillScale = data.homePillScale,
        homeStatusWidthFraction = data.homeStatusWidthFraction,
        homeCardImageStyle = data.homeCardImageStyle,
        showHomeCardImageWithoutBackground = data.showHomeCardImageWithoutBackground,
        uiMode = data.uiMode,
        enableBlur = data.enableBlur,
        enableProgressiveBlur = data.enableProgressiveBlur,
        enableFloatingBottomBar = data.enableFloatingBottomBar,
        enableFloatingBottomBarBlur = data.enableFloatingBottomBarBlur,
        enableNavigationBadge = data.enableNavigationBadge,
        enablePredictiveBack = data.enablePredictiveBack,
        enableSwipeDismiss = data.enableSwipeDismiss,
        pagerInterceptionMode = data.pagerInterceptionMode,
        pageScale = data.pageScale,
        moduleDescriptionMaxLines = data.moduleDescriptionMaxLines,
        // Module sort in adaptive packages does not switch with variants (aligns with SukiSU: top-level moduleSort* are masked).
        moduleSortEnabledFirst = null,
        moduleSortActionFirst = null,
        moduleSortWebUiFirst = null,
        moduleSortModuleFirst = null,
        moduleSortMetamoduleFirst = null,
        moduleSortNameOrder = null,
        moduleSortManual = null,
    )

    /** Write color fields to existing keys. Adaptive variants do not force light/dark: follow-system mode stays following. */
    private fun writeColorFields(
        p: AppearanceProjection,
        adaptive: Boolean,
        options: ThemeApplyOptions,
    ) {
        if (options.colors) {
            prefs.edit {
                p.miuixMonet?.let { putBoolean(ThemePrefKeys.MIUIX_MONET, it) }
                p.keyColor?.let { putInt(ThemePrefKeys.PREF_COLOR, it) }
                p.colorStyle?.takeIf(::isValidPaletteStyle)?.let {
                    putString(ThemePrefKeys.COLOR_STYLE, it)
                }
                p.colorSpec?.takeIf(::isValidColorSpec)?.let {
                    putString(ThemePrefKeys.COLOR_SPEC, it)
                }
                val mode = p.themeMode?.takeIf { it in 0..COLOR_MODE_MAX } ?: return@edit
                if (adaptive && isSystemFollowMode(prefs.getInt(ThemePrefKeys.COLOR_MODE, 0))) {
                    // Follow-system mode only updates the monet flavor; does not pin the user to light/dark.
                    val monet = p.miuixMonet ?: prefs.getBoolean(ThemePrefKeys.MIUIX_MONET, false)
                    putInt(
                        ThemePrefKeys.COLOR_MODE,
                        if (monet) COLOR_MODE_MONET_SYSTEM else COLOR_MODE_SYSTEM,
                    )
                } else {
                    putInt(ThemePrefKeys.COLOR_MODE, mode)
                }
            }
        }
        if (options.interfaceMode) {
            prefs.edit {
                p.uiMode?.takeIf { it == UI_MODE_MIUIX || it == UI_MODE_MATERIAL }
                    ?.let { putString(ThemePrefKeys.UI_MODE, it) }
            }
        }
    }

    /**
     * Background image/card image/home display fields. Image paths use the
     * fixed-name layout; images are promoted before settings are written on
     * apply, so paths can be derived directly from the directory. [dark] null
     * means the plain package top level; true/false means an adaptive variant.
     * Every group is written only when its dialog option is selected.
     */
    private fun writeAppearanceFields(
        p: AppearanceProjection,
        dark: Boolean?,
        options: ThemeApplyOptions,
    ) {
        if (options.backgroundImage) {
            // When the package explicitly declares "no image", delete the old file too.
            if (p.hasBackground == false) {
                ThemeImageStore.backgroundFile(context, dark).delete()
            }
            prefs.edit {
                when (p.hasBackground) {
                    true -> putString(
                        ThemePrefKeys.THEME_BACKGROUND_PATH,
                        if (dark == null) {
                            ThemeImageStore.backgroundFile(context, null).absolutePath
                        } else {
                            ""
                        },
                    )

                    false -> putString(ThemePrefKeys.THEME_BACKGROUND_PATH, "")
                    else -> {}
                }
                p.hasBackground?.let { putBoolean(ThemePrefKeys.THEME_HAS_BACKGROUND, it) }
            }
        }
        if (options.homeCardImage) {
            if (p.hasCardBackground == false) {
                ThemeImageStore.cardBackgroundFile(context, dark).delete()
            }
            prefs.edit {
                when (p.hasCardBackground) {
                    true -> putString(
                        ThemePrefKeys.THEME_CARD_BACKGROUND_PATH,
                        if (dark == null) {
                            ThemeImageStore.cardBackgroundFile(context, null).absolutePath
                        } else {
                            ""
                        },
                    )

                    false -> putString(ThemePrefKeys.THEME_CARD_BACKGROUND_PATH, "")
                    else -> {}
                }
                p.hasCardBackground?.let {
                    putBoolean(ThemePrefKeys.THEME_HAS_CARD_BACKGROUND, it)
                }
            }
        }
        if (options.backgroundAppearance) {
            prefs.edit {
                p.cardBackgroundAlphaPercent?.let {
                    putInt(
                        ThemePrefKeys.THEME_CARD_BG_ALPHA_PERCENT,
                        it.coerceIn(CARD_ALPHA_MIN, CARD_ALPHA_MAX),
                    )
                }
                p.customBackgroundBlurPercent?.let {
                    putInt(ThemePrefKeys.THEME_BG_BLUR_PERCENT, it.coerceIn(BLUR_MIN, BLUR_MAX))
                }
                p.customBackgroundScrimPercent?.let {
                    putInt(ThemePrefKeys.THEME_BG_SCRIM_PERCENT, it.coerceIn(SCRIM_MIN, SCRIM_MAX))
                }
            }
        }
        if (options.homeCardDisplay) {
            prefs.edit {
                p.hideHomeCardStatusText?.let {
                    putBoolean(ThemePrefKeys.THEME_HIDE_HOME_CARD_STATUS_TEXT, it)
                }
                p.hideHomeCardDecoration?.let {
                    putBoolean(ThemePrefKeys.THEME_HIDE_HOME_CARD_DECORATION, it)
                }
                p.homeLayout?.let {
                    putString(ThemePrefKeys.THEME_HOME_LAYOUT, HomeLayout.fromValue(it).value)
                }
                p.homeStatusAlignment?.let {
                    putString(
                        ThemePrefKeys.THEME_HOME_STATUS_ALIGNMENT,
                        HomeStatusAlignment.fromValue(it).value,
                    )
                }
                p.homeStatusScale?.let {
                    putFloat(ThemePrefKeys.THEME_HOME_STATUS_SCALE, HomeStatusStyle.coerceScale(it))
                }
                p.homePillScale?.let {
                    putFloat(
                        ThemePrefKeys.THEME_HOME_PILL_SCALE,
                        HomeStatusStyle.coercePillScale(it),
                    )
                }
                p.homeStatusWidthFraction?.let {
                    putFloat(
                        ThemePrefKeys.THEME_HOME_STATUS_WIDTH_FRACTION,
                        HomeStatusStyle.coerceWidth(it),
                    )
                }
                p.homeCardImageStyle?.let {
                    putString(
                        ThemePrefKeys.THEME_HOME_CARD_IMAGE_STYLE,
                        HomeCardImageStyle.fromValue(it).value,
                    )
                }
                p.showHomeCardImageWithoutBackground?.let {
                    putBoolean(ThemePrefKeys.THEME_SHOW_HOME_CARD_IMAGE_WITHOUT_BACKGROUND, it)
                }
            }
        }
        if (options.stickers) {
            prefs.edit {
                p.homeCardStickerTransforms?.let { transforms ->
                    putString(
                        ThemePrefKeys.THEME_STICKER_TRANSFORMS,
                        json.encodeToString<List<HomeCardStickerTransform>>(transforms),
                    )
                }
            }
        }
    }

    /**
     * Switch/interaction/module display fields: existing switch keys are written
     * directly, the rest follow the theme_ convention. Each group is written
     * only when its dialog option is selected.
     */
    private fun writeChromeFields(p: AppearanceProjection, options: ThemeApplyOptions) {
        if (options.interfaceSwitches) {
            prefs.edit {
                p.enableBlur?.let { putBoolean(ThemePrefKeys.ENABLE_BLUR, it) }
                p.enableProgressiveBlur?.let {
                    putBoolean(ThemePrefKeys.THEME_ENABLE_PROGRESSIVE_BLUR, it)
                }
                p.enableFloatingBottomBar?.let {
                    putBoolean(ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR, it)
                }
                p.enableFloatingBottomBarBlur?.let {
                    putBoolean(ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR_BLUR, it)
                }
                p.enableNavigationBadge?.let {
                    putBoolean(ThemePrefKeys.THEME_ENABLE_NAVIGATION_BADGE, it)
                }
            }
        }
        if (options.interactionBehavior) {
            prefs.edit {
                p.enablePredictiveBack?.let {
                    // Double write: the existing key takes effect via AeFoxApp/SettingsRepository (after restart); the theme_ key is a record.
                    putBoolean(ThemePrefKeys.ENABLE_PREDICTIVE_BACK, it)
                    putBoolean(ThemePrefKeys.THEME_ENABLE_PREDICTIVE_BACK, it)
                }
                p.enableSwipeDismiss?.let {
                    putBoolean(ThemePrefKeys.THEME_ENABLE_SWIPE_DISMISS, it)
                }
                p.pagerInterceptionMode?.let {
                    putInt(ThemePrefKeys.THEME_PAGER_INTERCEPTION_MODE, it.coerceIn(0, 2))
                }
                p.pageScale?.let {
                    putFloat(ThemePrefKeys.THEME_PAGE_SCALE, it.coerceIn(0.8f, 1.1f))
                }
            }
        }
        if (options.moduleDisplay) {
            prefs.edit {
                p.moduleDescriptionMaxLines?.let {
                    putInt(ThemePrefKeys.THEME_MODULE_DESCRIPTION_MAX_LINES, it.coerceIn(1, 5))
                }
                p.moduleSortEnabledFirst?.let {
                    putBoolean(ThemePrefKeys.THEME_MODULE_SORT_ENABLED_FIRST, it)
                }
                p.moduleSortActionFirst?.let {
                    putBoolean(ThemePrefKeys.THEME_MODULE_SORT_ACTION_FIRST, it)
                }
                p.moduleSortWebUiFirst?.let {
                    putBoolean(ThemePrefKeys.THEME_MODULE_SORT_WEB_UI_FIRST, it)
                }
                p.moduleSortModuleFirst?.let {
                    putBoolean(ThemePrefKeys.THEME_MODULE_SORT_MODULE_FIRST, it)
                }
                p.moduleSortMetamoduleFirst?.let {
                    putBoolean(ThemePrefKeys.THEME_MODULE_SORT_METAMODULE_FIRST, it)
                }
                p.moduleSortNameOrder?.takeIf { it in MODULE_NAME_ORDERS }?.let {
                    putString(ThemePrefKeys.THEME_MODULE_SORT_NAME_ORDER, it)
                }
                p.moduleSortManual?.let { putBoolean(ThemePrefKeys.THEME_MODULE_SORT_MANUAL, it) }
            }
        }
    }

    /** Top-level app-sorting fields (global for adaptive packages). */
    private fun writeModuleSortFields(data: ThemePackageData) {
        prefs.edit {
            data.moduleSortEnabledFirst?.let {
                putBoolean(ThemePrefKeys.THEME_MODULE_SORT_ENABLED_FIRST, it)
            }
            data.moduleSortActionFirst?.let {
                putBoolean(ThemePrefKeys.THEME_MODULE_SORT_ACTION_FIRST, it)
            }
            data.moduleSortWebUiFirst?.let {
                putBoolean(ThemePrefKeys.THEME_MODULE_SORT_WEB_UI_FIRST, it)
            }
            data.moduleSortModuleFirst?.let {
                putBoolean(ThemePrefKeys.THEME_MODULE_SORT_MODULE_FIRST, it)
            }
            data.moduleSortMetamoduleFirst?.let {
                putBoolean(ThemePrefKeys.THEME_MODULE_SORT_METAMODULE_FIRST, it)
            }
            data.moduleSortNameOrder?.takeIf { it in MODULE_NAME_ORDERS }?.let {
                putString(ThemePrefKeys.THEME_MODULE_SORT_NAME_ORDER, it)
            }
            data.moduleSortManual?.let { putBoolean(ThemePrefKeys.THEME_MODULE_SORT_MANUAL, it) }
        }
    }

    // Brand adaptation: imported packages may embed "SukiSU (Ultra)" wording in user-facing
    // texts, so the swap happens in memory at apply time:
    // "SukiSU Ultra" (any casing/spacing) collapses to the brand word first, then a bare
    // "SukiSU"; every other custom wording is preserved untouched. Keys are never rewritten.
    private val brandUltraPattern = Regex("(?i)sukisu\\s*ultra")
    private val brandPattern = Regex("(?i)sukisu")

    private fun rebrandBrandWord(text: String): String =
        if (!brandPattern.containsMatchIn(text)) {
            text
        } else {
            brandPattern.replace(brandUltraPattern.replace(text, "AeFox"), "AeFox")
        }

    /**
     * Global group: custom text/title + appearance overlay + bottom bar icons +
     * launcher icon. Each sub-group is written only when its dialog option is
     * selected; unchecked or absent data keeps the current values.
     */
    private fun writeGlobalFields(
        data: ThemePackageData,
        promoted: PromotedImages,
        options: ThemeApplyOptions,
    ) {
        prefs.edit {
            if (options.customTexts) {
                val incomingTexts = customTextsInGroups(
                    customTexts = data.customTexts.orEmpty(),
                    groups = options.customTextGroups,
                )
                    .filterValues { it.isNotBlank() }
                    .mapValues { (_, value) -> rebrandBrandWord(value) }
                if (incomingTexts.isNotEmpty()) {
                    // Replace only the selected groups; unselected groups keep their current text.
                    val merged = decodeStringMap(prefs.getString(ThemePrefKeys.THEME_CUSTOM_TEXTS, null))
                        .toMutableMap()
                    merged.keys.toList().forEach { key ->
                        val group = customTextGroupOf(key)
                        if (group != null && group in options.customTextGroups) merged.remove(key)
                    }
                    merged.putAll(incomingTexts)
                    putString(
                        ThemePrefKeys.THEME_CUSTOM_TEXTS,
                        json.encodeToString<Map<String, String>>(merged),
                    )
                }
                // Absent home title keeps the current one; a present title replaces it.
                if (CustomTextGroup.HOME in options.customTextGroups) {
                    data.customHomeTitle?.takeIf { it.isNotBlank() }?.let {
                        putString(ThemePrefKeys.THEME_CUSTOM_HOME_TITLE, rebrandBrandWord(it))
                    }
                }
            }
            if (options.customColors || options.imageEffects) {
                val incoming = data.appearance
                val applyColors = options.customColors && incoming?.colors != null
                val applyImageEffects = options.imageEffects && incoming?.image != null
                if (applyColors || applyImageEffects) {
                    val merged = AppearanceOverlayStore.merge(
                        current = readAppearanceOverlay(),
                        incoming = incoming,
                        applyColors = applyColors,
                        applyImageEffects = applyImageEffects,
                    )
                    putString(
                        ThemePrefKeys.THEME_APPEARANCE,
                        merged?.let { AppearanceOverlayStore.encode(it) } ?: "",
                    )
                }
            }
            // The field doubles as the "package has icons" marker: no list and no
            // promoted file means the package carries no icon data, keep the current state.
            if (
                options.bottomBarIcons &&
                (data.bottomBarIcons != null || promoted.bottomBarIcons.isNotEmpty())
            ) {
                putString(
                    ThemePrefKeys.THEME_BOTTOM_BAR_ICONS,
                    json.encodeToString<Map<String, String>>(promoted.bottomBarIcons),
                )
            }
            if (options.appIcon) {
                data.appIconId?.takeIf { it.isNotBlank() }?.let {
                    putString(ThemePrefKeys.THEME_APP_ICON_ID, it)
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Image promotion
    // ------------------------------------------------------------------

    /** Promotion result: bottom bar icon map + adaptive variant directories (empty string for plain packages). */
    private class PromotedImages(
        val bottomBarIcons: Map<String, String>,
        val lightDir: String,
        val darkDir: String,
    )

    /**
     * Promote imported temporary images into the fixed-name layout under
     * filesDir/theme. Each image group is promoted only when its dialog option
     * is selected; unselected groups keep the current files and their temporary
     * copies are removed at the cleanup stage.
     */
    private fun promoteImages(
        imported: ImportedTheme,
        options: ThemeApplyOptions,
    ): PromotedImages {
        val bottomBarIcons = if (
            options.bottomBarIcons &&
            (imported.data.bottomBarIcons != null || imported.bottomBarIconPaths.isNotEmpty())
        ) {
            ThemeImageStore.clearBottomBarIcons(context)
            promoteBottomBarIcons(imported)
        } else {
            emptyMap()
        }
        if (!imported.isAdaptive) {
            promotePlain(imported, options)
            return PromotedImages(bottomBarIcons, lightDir = "", darkDir = "")
        }
        if (!usesAdaptiveVariant(options)) {
            return PromotedImages(bottomBarIcons, lightDir = "", darkDir = "")
        }
        val lightDir = promoteVariant(imported.light!!, dark = false, options = options)
        val darkDir = promoteVariant(imported.dark!!, dark = true, options = options)
        return PromotedImages(bottomBarIcons, lightDir, darkDir)
    }

    private fun promoteBottomBarIcons(imported: ImportedTheme): Map<String, String> =
        imported.bottomBarIconPaths.mapNotNull { (slot, path) ->
            if (BottomBarIconSlot.fromKey(slot) == null) return@mapNotNull null
            val target = ThemeImageStore.bottomBarIconFile(context, slot)
            slot to ThemeImageStore.promote(File(path), target)
        }.toMap()

    private fun promotePlain(imported: ImportedTheme, options: ThemeApplyOptions) {
        if (options.backgroundImage) {
            promoteBackground(imported.backgroundPath, null, imported.data.hasBackground)
        }
        if (options.homeCardImage) {
            promoteCardBackground(imported.cardBackgroundPath, null, imported.data.hasCardBackground)
        }
        if (options.stickers) {
            promoteStickers(
                paths = imported.stickerPaths,
                dark = null,
                transforms = imported.data.homeCardStickerTransforms,
            )
        }
    }

    private fun promoteVariant(
        variant: ImportedThemeVariant,
        dark: Boolean,
        options: ThemeApplyOptions,
    ): String {
        if (options.backgroundImage) {
            promoteBackground(variant.backgroundPath, dark, variant.data.hasBackground)
        }
        if (options.homeCardImage) {
            promoteCardBackground(variant.cardBackgroundPath, dark, variant.data.hasCardBackground)
        }
        if (options.stickers) {
            promoteStickers(
                paths = variant.stickerPaths,
                dark = dark,
                transforms = variant.data.homeCardStickerTransforms,
            )
        }
        // Preserve the current values of every unselected group so the variant
        // snapshot used by export/adaptive restore keeps describing what is
        // actually applied.
        val current = ThemeImageStore.readVariantData(context, dark)
        val incoming = variant.data
        fun <T> selected(option: Boolean, value: T?, fallback: T?): T? =
            if (option) value else fallback

        val data = incoming.copy(
            themeMode = selected(options.colors, incoming.themeMode, current?.themeMode),
            miuixMonet = selected(options.colors, incoming.miuixMonet, current?.miuixMonet),
            keyColor = selected(options.colors, incoming.keyColor, current?.keyColor),
            colorStyle = selected(options.colors, incoming.colorStyle, current?.colorStyle),
            colorSpec = selected(options.colors, incoming.colorSpec, current?.colorSpec),
            cardBackgroundAlphaPercent = selected(
                options.backgroundAppearance,
                incoming.cardBackgroundAlphaPercent,
                current?.cardBackgroundAlphaPercent,
            ),
            customBackgroundBlurPercent = selected(
                options.backgroundAppearance,
                incoming.customBackgroundBlurPercent,
                current?.customBackgroundBlurPercent,
            ),
            customBackgroundScrimPercent = selected(
                options.backgroundAppearance,
                incoming.customBackgroundScrimPercent,
                current?.customBackgroundScrimPercent,
            ),
            hasBackground = selected(
                options.backgroundImage,
                incoming.hasBackground,
                current?.hasBackground,
            ),
            hasCardBackground = selected(
                options.homeCardImage,
                incoming.hasCardBackground,
                current?.hasCardBackground,
            ),
            hideHomeCardStatusText = selected(
                options.homeCardDisplay,
                incoming.hideHomeCardStatusText,
                current?.hideHomeCardStatusText,
            ),
            hideHomeCardDecoration = selected(
                options.homeCardDisplay,
                incoming.hideHomeCardDecoration,
                current?.hideHomeCardDecoration,
            ),
            homeCardStickerTransforms = selected(
                options.stickers,
                incoming.homeCardStickerTransforms,
                current?.homeCardStickerTransforms,
            ),
            homeLayout = selected(
                options.homeCardDisplay,
                incoming.homeLayout,
                current?.homeLayout,
            ),
            homeStatusAlignment = selected(
                options.homeCardDisplay,
                incoming.homeStatusAlignment,
                current?.homeStatusAlignment,
            ),
            homeStatusScale = selected(
                options.homeCardDisplay,
                incoming.homeStatusScale,
                current?.homeStatusScale,
            ),
            homePillScale = selected(
                options.homeCardDisplay,
                incoming.homePillScale,
                current?.homePillScale,
            ),
            homeStatusWidthFraction = selected(
                options.homeCardDisplay,
                incoming.homeStatusWidthFraction,
                current?.homeStatusWidthFraction,
            ),
            homeCardImageStyle = selected(
                options.homeCardDisplay,
                incoming.homeCardImageStyle,
                current?.homeCardImageStyle,
            ),
            showHomeCardImageWithoutBackground = selected(
                options.homeCardDisplay,
                incoming.showHomeCardImageWithoutBackground,
                current?.showHomeCardImageWithoutBackground,
            ),
            uiMode = selected(options.interfaceMode, incoming.uiMode, current?.uiMode),
            enableFloatingBottomBar = selected(
                options.interfaceSwitches,
                incoming.enableFloatingBottomBar,
                current?.enableFloatingBottomBar,
            ),
            enableFloatingBottomBarBlur = selected(
                options.interfaceSwitches,
                incoming.enableFloatingBottomBarBlur,
                current?.enableFloatingBottomBarBlur,
            ),
            enableNavigationBadge = selected(
                options.interfaceSwitches,
                incoming.enableNavigationBadge,
                current?.enableNavigationBadge,
            ),
            enablePredictiveBack = selected(
                options.interactionBehavior,
                incoming.enablePredictiveBack,
                current?.enablePredictiveBack,
            ),
            enableSwipeDismiss = selected(
                options.interactionBehavior,
                incoming.enableSwipeDismiss,
                current?.enableSwipeDismiss,
            ),
            enableBlur = selected(
                options.interfaceSwitches,
                incoming.enableBlur,
                current?.enableBlur,
            ),
            enableProgressiveBlur = selected(
                options.interfaceSwitches,
                incoming.enableProgressiveBlur,
                current?.enableProgressiveBlur,
            ),
            pagerInterceptionMode = selected(
                options.interactionBehavior,
                incoming.pagerInterceptionMode,
                current?.pagerInterceptionMode,
            ),
            pageScale = selected(
                options.interactionBehavior,
                incoming.pageScale,
                current?.pageScale,
            ),
            moduleDescriptionMaxLines = selected(
                options.moduleDisplay,
                incoming.moduleDescriptionMaxLines,
                current?.moduleDescriptionMaxLines,
            ),
        )
        ThemeImageStore.writeVariantData(context, dark, data)
        return ThemeImageStore.variantDir(context, dark).absolutePath
    }

    /**
     * Background image promotion. A declared "no image" removes the target;
     * absent data (null flag and no file) leaves the current image untouched.
     */
    private fun promoteBackground(path: String?, dark: Boolean?, hasImage: Boolean?) {
        val target = ThemeImageStore.backgroundFile(context, dark)
        when {
            hasImage == false -> target.delete()
            path != null -> ThemeImageStore.promote(File(path), target)
            else -> Unit
        }
    }

    private fun promoteCardBackground(path: String?, dark: Boolean?, hasImage: Boolean?) {
        val target = ThemeImageStore.cardBackgroundFile(context, dark)
        when {
            hasImage == false -> target.delete()
            path != null -> ThemeImageStore.promote(File(path), target)
            else -> Unit
        }
    }

    /**
     * Sticker promotion. A package that carries sticker data replaces the whole
     * set (files beyond the new count must not linger); a package without any
     * sticker data leaves the current stickers untouched.
     */
    private fun promoteStickers(
        paths: List<String>,
        dark: Boolean?,
        transforms: List<HomeCardStickerTransform>?,
    ) {
        if (paths.isEmpty() && transforms == null) return
        (0 until HomeCardSticker.MAX_COUNT).forEach { index ->
            ThemeImageStore.stickerFile(context, index, dark).delete()
        }
        paths.forEachIndexed { index, path ->
            ThemeImageStore.promote(File(path), ThemeImageStore.stickerFile(context, index, dark))
        }
    }

    // ------------------------------------------------------------------
    // Current appearance export
    // ------------------------------------------------------------------

    private class CurrentPayload(
        val data: ThemePackageData,
        val backgroundFile: File?,
        val cardBackgroundFile: File?,
        val stickerFiles: List<File>,
        val adaptive: ThemePackageAdaptiveExport?,
        val bottomBarIconFiles: Map<String, File>,
    )

    /** Rebuild the export payload for the current appearance from prefs + already-promoted images. */
    private fun buildCurrentPayload(
        adaptive: Boolean,
        sections: Set<ThemeApplySection>,
    ): CurrentPayload {
        val includeImages = ThemeApplySection.IMAGES in sections
        val bottomBarIconFiles = if (includeImages) readBottomBarIconFiles() else emptyMap()
        val data = buildCurrentData(bottomBarIconFiles.keys.toList(), readAppearanceOverlay())
            .forExportSections(sections)
        if (!adaptive) {
            val background = if (includeImages) {
                ThemeImageStore.backgroundFile(context, null).takeIf { it.isFile }
            } else {
                null
            }
            val card = if (includeImages) {
                ThemeImageStore.cardBackgroundFile(context, null).takeIf { it.isFile }
            } else {
                null
            }
            return CurrentPayload(
                if (includeImages) {
                    data.copy(
                        hasBackground = background != null,
                        hasCardBackground = card != null,
                    )
                } else {
                    data
                },
                background,
                card,
                if (includeImages) readStickerFiles(null) else emptyList(),
                null,
                bottomBarIconFiles,
            )
        }
        val export = ThemePackageAdaptiveExport(
            light = buildVariantExport(dark = false).forExportSections(sections),
            dark = buildVariantExport(dark = true).forExportSections(sections),
        )
        return CurrentPayload(data, null, null, emptyList(), export, bottomBarIconFiles)
    }

    /** Drops the groups the user did not select from the exported top-level data. */
    private fun ThemePackageData.forExportSections(
        sections: Set<ThemeApplySection>,
    ): ThemePackageData {
        val colors = ThemeApplySection.COLORS in sections
        val images = ThemeApplySection.IMAGES in sections
        val texts = ThemeApplySection.TEXTS in sections
        val overlay = ThemeApplySection.OVERLAY in sections
        val sort = ThemeApplySection.MODULE_SORT in sections
        return copy(
            themeMode = themeMode.takeIf { colors },
            miuixMonet = miuixMonet.takeIf { colors },
            keyColor = keyColor.takeIf { colors },
            colorStyle = colorStyle.takeIf { colors },
            colorSpec = colorSpec.takeIf { colors },
            uiMode = uiMode.takeIf { colors },
            cardBackgroundAlphaPercent = cardBackgroundAlphaPercent.takeIf { images },
            customBackgroundBlurPercent = customBackgroundBlurPercent.takeIf { images },
            customBackgroundScrimPercent = customBackgroundScrimPercent.takeIf { images },
            hasBackground = hasBackground.takeIf { images },
            hasCardBackground = hasCardBackground.takeIf { images },
            hideHomeCardStatusText = hideHomeCardStatusText.takeIf { images },
            hideHomeCardDecoration = hideHomeCardDecoration.takeIf { images },
            homeCardStickerTransforms = homeCardStickerTransforms.takeIf { images },
            homeLayout = homeLayout.takeIf { images },
            homeStatusAlignment = homeStatusAlignment.takeIf { images },
            homeStatusScale = homeStatusScale.takeIf { images },
            homePillScale = homePillScale.takeIf { images },
            homeStatusWidthFraction = homeStatusWidthFraction.takeIf { images },
            homeCardImageStyle = homeCardImageStyle.takeIf { images },
            showHomeCardImageWithoutBackground = showHomeCardImageWithoutBackground.takeIf { images },
            bottomBarIcons = bottomBarIcons.takeIf { images },
            customHomeTitle = customHomeTitle.takeIf { texts },
            customTexts = customTexts.takeIf { texts },
            appearance = appearance.takeIf { overlay },
            moduleSortEnabledFirst = moduleSortEnabledFirst.takeIf { sort },
            moduleSortActionFirst = moduleSortActionFirst.takeIf { sort },
            moduleSortWebUiFirst = moduleSortWebUiFirst.takeIf { sort },
            moduleSortModuleFirst = moduleSortModuleFirst.takeIf { sort },
            moduleSortMetamoduleFirst = moduleSortMetamoduleFirst.takeIf { sort },
            moduleSortNameOrder = moduleSortNameOrder.takeIf { sort },
            moduleSortManual = moduleSortManual.takeIf { sort },
        )
    }

    /** Same section filtering for the adaptive light/dark variant halves. */
    private fun ThemePackageVariantExport.forExportSections(
        sections: Set<ThemeApplySection>,
    ): ThemePackageVariantExport {
        val colors = ThemeApplySection.COLORS in sections
        val images = ThemeApplySection.IMAGES in sections
        return copy(
            data = data.copy(
                themeMode = data.themeMode.takeIf { colors },
                miuixMonet = data.miuixMonet.takeIf { colors },
                keyColor = data.keyColor.takeIf { colors },
                colorStyle = data.colorStyle.takeIf { colors },
                colorSpec = data.colorSpec.takeIf { colors },
                uiMode = data.uiMode.takeIf { colors },
                cardBackgroundAlphaPercent = data.cardBackgroundAlphaPercent.takeIf { images },
                customBackgroundBlurPercent = data.customBackgroundBlurPercent.takeIf { images },
                customBackgroundScrimPercent = data.customBackgroundScrimPercent.takeIf { images },
                hasBackground = data.hasBackground.takeIf { images },
                hasCardBackground = data.hasCardBackground.takeIf { images },
                hideHomeCardStatusText = data.hideHomeCardStatusText.takeIf { images },
                hideHomeCardDecoration = data.hideHomeCardDecoration.takeIf { images },
                homeCardStickerTransforms = data.homeCardStickerTransforms.takeIf { images },
                homeLayout = data.homeLayout.takeIf { images },
                homeStatusAlignment = data.homeStatusAlignment.takeIf { images },
                homeStatusScale = data.homeStatusScale.takeIf { images },
                homePillScale = data.homePillScale.takeIf { images },
                homeStatusWidthFraction = data.homeStatusWidthFraction.takeIf { images },
                homeCardImageStyle = data.homeCardImageStyle.takeIf { images },
                showHomeCardImageWithoutBackground =
                    data.showHomeCardImageWithoutBackground.takeIf { images },
            ),
            backgroundFile = backgroundFile.takeIf { images },
            cardBackgroundFile = cardBackgroundFile.takeIf { images },
            cardStickerFiles = cardStickerFiles.takeIf { images } ?: emptyList(),
        )
    }

    /** Rebuild the package top-level data (colors + chrome + global + home display); image flags are left to the writer side when adaptive. */
    private fun buildCurrentData(
        bottomBarSlots: List<String>,
        appearance: AppearanceOverlayData?,
    ): ThemePackageData {
        val appearanceData = buildCurrentAppearanceData(bottomBarSlots, appearance)
        return appearanceData.copy(
            uiMode = stringPref(ThemePrefKeys.UI_MODE, UI_MODE_MIUIX),
            enableBlur = boolPref(ThemePrefKeys.ENABLE_BLUR, true),
            enableProgressiveBlur = boolPref(ThemePrefKeys.THEME_ENABLE_PROGRESSIVE_BLUR, false),
            enableFloatingBottomBar = boolPref(ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR, true),
            enableFloatingBottomBarBlur = boolPref(
                ThemePrefKeys.ENABLE_FLOATING_BOTTOM_BAR_BLUR,
                true,
            ),
            enableNavigationBadge = boolPref(ThemePrefKeys.THEME_ENABLE_NAVIGATION_BADGE, true),
            enablePredictiveBack = boolPref(ThemePrefKeys.ENABLE_PREDICTIVE_BACK, true),
            enableSwipeDismiss = boolPref(ThemePrefKeys.THEME_ENABLE_SWIPE_DISMISS, true),
            pagerInterceptionMode = intPref(ThemePrefKeys.THEME_PAGER_INTERCEPTION_MODE, 1),
            pageScale = floatPref(ThemePrefKeys.THEME_PAGE_SCALE, 1f),
            moduleDescriptionMaxLines = intPref(ThemePrefKeys.THEME_MODULE_DESCRIPTION_MAX_LINES, 4),
            moduleSortEnabledFirst = boolPref(ThemePrefKeys.THEME_MODULE_SORT_ENABLED_FIRST, false),
            moduleSortActionFirst = boolPref(ThemePrefKeys.THEME_MODULE_SORT_ACTION_FIRST, false),
            moduleSortWebUiFirst = boolPref(ThemePrefKeys.THEME_MODULE_SORT_WEB_UI_FIRST, false),
            moduleSortModuleFirst = boolPref(ThemePrefKeys.THEME_MODULE_SORT_MODULE_FIRST, false),
            moduleSortMetamoduleFirst = boolPref(
                ThemePrefKeys.THEME_MODULE_SORT_METAMODULE_FIRST,
                false,
            ),
            moduleSortNameOrder = stringPref(ThemePrefKeys.THEME_MODULE_SORT_NAME_ORDER, MODULE_NAME_DEFAULT),
            moduleSortManual = boolPref(ThemePrefKeys.THEME_MODULE_SORT_MANUAL, false),
        )
    }

    /** Color/background/home display/global parts of the top-level data. */
    private fun buildCurrentAppearanceData(
        bottomBarSlots: List<String>,
        appearance: AppearanceOverlayData?,
    ): ThemePackageData = ThemePackageData(
        version = ThemePackageStorage.BASE_VERSION,
        themeMode = intPref(ThemePrefKeys.COLOR_MODE, COLOR_MODE_SYSTEM),
        miuixMonet = boolPref(ThemePrefKeys.MIUIX_MONET, false),
        keyColor = intPref(ThemePrefKeys.PREF_COLOR, 0),
        colorStyle = stringPref(ThemePrefKeys.COLOR_STYLE, PaletteStyle.TonalSpot.name),
        colorSpec = stringPref(ThemePrefKeys.COLOR_SPEC, ColorSpec.SpecVersion.Default.name),
        cardBackgroundAlphaPercent = intPref(ThemePrefKeys.THEME_CARD_BG_ALPHA_PERCENT, DEFAULT_CARD_ALPHA),
        customBackgroundBlurPercent = intPref(ThemePrefKeys.THEME_BG_BLUR_PERCENT, DEFAULT_BACKGROUND_BLUR),
        customBackgroundScrimPercent = intPref(ThemePrefKeys.THEME_BG_SCRIM_PERCENT, DEFAULT_BACKGROUND_SCRIM),
        hasBackground = null,
        hasCardBackground = null,
        hideHomeCardStatusText = boolPref(ThemePrefKeys.THEME_HIDE_HOME_CARD_STATUS_TEXT, false),
        hideHomeCardDecoration = boolPref(ThemePrefKeys.THEME_HIDE_HOME_CARD_DECORATION, false),
        homeCardStickerTransforms = readStickerTransforms(),
        homeLayout = stringPref(ThemePrefKeys.THEME_HOME_LAYOUT, HomeLayout.CLASSIC.value),
        homeStatusAlignment = stringPref(
            ThemePrefKeys.THEME_HOME_STATUS_ALIGNMENT,
            HomeStatusAlignment.START.value,
        ),
        homeStatusScale = HomeStatusStyle.coerceScale(
            floatPref(ThemePrefKeys.THEME_HOME_STATUS_SCALE, HomeStatusStyle.SCALE_DEFAULT),
        ),
        homePillScale = HomeStatusStyle.coercePillScale(
            floatPref(ThemePrefKeys.THEME_HOME_PILL_SCALE, HomeStatusStyle.PILL_SCALE_DEFAULT),
        ),
        homeStatusWidthFraction = HomeStatusStyle.coerceWidth(
            floatPref(ThemePrefKeys.THEME_HOME_STATUS_WIDTH_FRACTION, HomeStatusStyle.WIDTH_DEFAULT),
        ),
        homeCardImageStyle = stringPref(
            ThemePrefKeys.THEME_HOME_CARD_IMAGE_STYLE,
            HomeCardImageStyle.BANNER.value,
        ),
        showHomeCardImageWithoutBackground = boolPref(
            ThemePrefKeys.THEME_SHOW_HOME_CARD_IMAGE_WITHOUT_BACKGROUND,
            false,
        ),
        customHomeTitle = prefs.getString(ThemePrefKeys.THEME_CUSTOM_HOME_TITLE, null)
            ?.takeIf { it.isNotBlank() },
        customTexts = decodeStringMap(prefs.getString(ThemePrefKeys.THEME_CUSTOM_TEXTS, null))
            .takeIf { it.isNotEmpty() },
        bottomBarIcons = bottomBarSlots.takeIf { it.isNotEmpty() },
        appearance = appearance,
        appIconId = prefs.getString(ThemePrefKeys.THEME_APP_ICON_ID, null)
            ?.takeIf { it.isNotBlank() },
    )

    private fun intPref(key: String, default: Int): Int = prefs.getInt(key, default)

    private fun boolPref(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    private fun floatPref(key: String, default: Float): Float = prefs.getFloat(key, default)

    private fun stringPref(key: String, default: String): String =
        prefs.getString(key, null) ?: default

    private fun buildVariantExport(dark: Boolean): ThemePackageVariantExport {
        val data = ThemeImageStore.readVariantData(context, dark)
            ?: error("Variant data is missing for ${if (dark) "dark" else "light"} mode")
        val background = if (data.hasBackground == true) {
            ThemeImageStore.backgroundFile(context, dark).takeIf { it.isFile }
        } else {
            null
        }
        val card = if (data.hasCardBackground == true) {
            ThemeImageStore.cardBackgroundFile(context, dark).takeIf { it.isFile }
        } else {
            null
        }
        return ThemePackageVariantExport(data, background, card, readStickerFiles(dark))
    }

    private fun readStickerFiles(dark: Boolean?): List<File> =
        (0 until HomeCardSticker.MAX_COUNT).mapNotNull { index ->
            ThemeImageStore.stickerFile(context, index, dark).takeIf { it.isFile }
        }

    private fun readStickerTransforms(): List<HomeCardStickerTransform>? {
        val raw = prefs.getString(ThemePrefKeys.THEME_STICKER_TRANSFORMS, null)
        if (raw.isNullOrBlank()) return null
        return runCatching {
            json.decodeFromString<List<HomeCardStickerTransform>>(raw)
        }.getOrNull()
    }

    private fun readBottomBarIconFiles(): Map<String, File> {
        val raw = prefs.getString(ThemePrefKeys.THEME_BOTTOM_BAR_ICONS, null) ?: return emptyMap()
        return decodeStringMap(raw).mapNotNull { (slot, path) ->
            val file = File(path)
            if (BottomBarIconSlot.fromKey(slot) == null || !file.isFile) {
                return@mapNotNull null
            }
            slot to file
        }.toMap()
    }

    private fun readAppearanceOverlay(): AppearanceOverlayData? {
        val raw = prefs.getString(ThemePrefKeys.THEME_APPEARANCE, null)
        if (raw.isNullOrBlank()) return null
        return runCatching {
            AppearanceOverlayStore.parse(raw).getOrThrow().data
        }.getOrNull()
    }

    private fun decodeStringMap(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
    }

    // ------------------------------------------------------------------
    // Active markers and state helpers
    // ------------------------------------------------------------------

    /** Active markers: id = SHA-256 of the canonical JSON of the package data; name = caller-provided display name. */
    private fun writeActiveMarkers(data: ThemePackageData, name: String) {
        val canonical = json.encodeToString(ThemePackageData.serializer(), data)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
        prefs.edit {
            putString(ThemePrefKeys.THEME_ACTIVE_ID, digest.joinToString("") { "%02x".format(it) })
            putString(
                ThemePrefKeys.THEME_ACTIVE_NAME,
                name.ifBlank {
                    "theme-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                },
            )
        }
    }

    private fun readActiveId(): String = prefs.getString(ThemePrefKeys.THEME_ACTIVE_ID, "") ?: ""

    private fun readActiveName(): String =
        prefs.getString(ThemePrefKeys.THEME_ACTIVE_NAME, "") ?: ""

    private fun isSystemDarkMode(): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun isSystemFollowMode(value: Int): Boolean =
        value == COLOR_MODE_SYSTEM || value == COLOR_MODE_MONET_SYSTEM

    private fun isValidPaletteStyle(name: String): Boolean =
        runCatching { PaletteStyle.valueOf(name) }.isSuccess

    private fun isValidColorSpec(name: String): Boolean =
        runCatching { ColorSpec.SpecVersion.valueOf(name) }.isSuccess

    private fun setStage(stage: ThemePackageStage) {
        val current = _state.value as? ThemePackageState.Working ?: return
        _state.value = current.copy(stage = stage)
    }

    private suspend fun <T> runOp(
        op: ThemePackageOp,
        initial: ThemePackageStage,
        block: suspend () -> T,
    ): Result<T> {
        _state.value = ThemePackageState.Working(op, initial)
        return try {
            val value = withContext(Dispatchers.IO) { block() }
            _state.value = ThemePackageState.Done(op)
            Result.success(value)
        } catch (cancelled: CancellationException) {
            _state.value = ThemePackageState.Idle
            throw cancelled
        } catch (error: Throwable) {
            val rolledBack = (error as? ThemeApplyException)?.rolledBack ?: false
            _state.value = ThemePackageState.Failed(
                op,
                error.message ?: error.javaClass.simpleName,
                rolledBack,
            )
            Result.failure(error)
        }
    }

    private companion object {
        /** Existing color_mode values (consistent with AeFox ColorMode: 0/3 mean follow system). */
        const val COLOR_MODE_SYSTEM = 0
        const val COLOR_MODE_MONET_SYSTEM = 3
        const val COLOR_MODE_MAX = 6
        const val UI_MODE_MIUIX = "miuix"
        const val UI_MODE_MATERIAL = "material"

        /** Background appearance parameter bounds (aligns with SukiSU ImageStorage constants). */
        const val CARD_ALPHA_MIN = 10
        const val CARD_ALPHA_MAX = 91
        const val DEFAULT_CARD_ALPHA = 60
        const val BLUR_MIN = 0
        const val BLUR_MAX = 100
        const val DEFAULT_BACKGROUND_BLUR = 50
        const val SCRIM_MIN = -100
        const val SCRIM_MAX = 100
        const val DEFAULT_BACKGROUND_SCRIM = -35

        /** Legal values for module name sorting (aligns with SukiSU ModuleNameSort). */
        val MODULE_NAME_ORDERS = setOf("DEFAULT", "ASCENDING", "DESCENDING")
        const val MODULE_NAME_DEFAULT = "DEFAULT"

        const val ROLLBACK_FILE_NAME = "theme_rollback.sukisu"
        const val TRIAL_SNAPSHOT_FILE_NAME = "theme_trial_snapshot.sukisu"
    }
}

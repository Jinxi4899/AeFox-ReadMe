package aefox.reyang.ui.screen.colorpalette

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import aefox.reyang.R
import aefox.reyang.AeFoxApp
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.component.LocalAeFoxToast
import aefox.reyang.ui.component.dialog.ConfirmResult
import aefox.reyang.ui.component.dialog.rememberConfirmDialog
import aefox.reyang.ui.navigation3.LocalNavigator
import aefox.reyang.ui.navigation3.Route
import aefox.reyang.ui.screen.themestore.ThemeApplyOptionsDialog
import aefox.reyang.ui.screen.themestore.ThemeStoreApplySectionDialog
import aefox.reyang.ui.theme.ColorMode
import aefox.reyang.ui.theme.themepackage.ThemeApplySection
import aefox.reyang.ui.theme.themepackage.ThemePackageOp
import aefox.reyang.ui.theme.themepackage.ThemePackageState
import aefox.reyang.ui.viewmodel.SettingsViewModel
import java.io.File
import kotlinx.coroutines.launch

/**
 * Appearance settings dispatcher: besides color/switch settings, hosts the theme package
 * entry (Task 6.5) -- SAF import of a local package (applied after confirm), export of
 * the current appearance, restore defaults, and trial state with cancel.
 */
@Composable
fun ColorPaletteScreen(onOpenAppearanceOverlay: () -> Unit) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val toast = LocalAeFoxToast.current
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentPaletteStyle = try {
        PaletteStyle.valueOf(uiState.colorStyle)
    } catch (_: Exception) {
        PaletteStyle.TonalSpot
    }
    val currentColorSpec = try {
        ColorSpec.SpecVersion.valueOf(uiState.colorSpec)
    } catch (_: Exception) {
        ColorSpec.SpecVersion.Default
    }
    val state = ColorPaletteUiState(
        uiState = uiState,
        currentColorMode = ColorMode.fromValue(uiState.themeMode),
        currentPaletteStyle = currentPaletteStyle,
        currentColorSpec = currentColorSpec,
    )

    val scope = rememberCoroutineScope()
    // A local package is parsed first; the fine-grained options dialog then decides what to apply.
    val pendingImport by viewModel.pendingImport.collectAsStateWithLifecycle()
    val pendingImportName by viewModel.pendingImportName.collectAsStateWithLifecycle()
    var showExportOptions by rememberSaveable { mutableStateOf(false) }
    var exportSections by remember { mutableStateOf(ThemeApplySection.ALL) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val file = copyUriToCacheFile(context, uri, "theme_import.sukisu")
            if (file != null) viewModel.stageThemeImport(file, file.name)
        }
    }
    // Export: await the export into a cache temp package, then copy into the SAF target.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                exportToUri(context, uri, viewModel, uiState.themeVisuals.adaptive, exportSections, toast::show)
            }
        }
    }
    val confirm = rememberConfirmDialog()
    val restoreTitle = stringResource(R.string.theme_pkg_restore_default_title)
    val restoreBody = stringResource(R.string.theme_pkg_restore_default_body)
    val confirmText = stringResource(R.string.theme_pkg_restore_default)
    val cancelText = stringResource(R.string.theme_pkg_cancel)

    // Restore-default needs a confirm dialog; block on a suspend call until the user chooses.
    LaunchedEffect(Unit) {
        viewModel.awaitRestoreRequest.collect {
            val result = confirm.awaitConfirm(
                title = restoreTitle,
                content = restoreBody,
                confirm = confirmText,
                dismiss = cancelText,
            )
            if (result is ConfirmResult.Confirmed) viewModel.restoreDefault(activity)
        }
    }

    // Toasts for theme package op results (trial start/failure, apply/export/restore success and failure).
    LaunchedEffect(Unit) {
        viewModel.themePackageState.collect { packageState ->
            when (packageState) {
                is ThemePackageState.Done -> toast.show(packageDoneText(context, packageState.op))
                is ThemePackageState.Failed -> toast.show(packageFailedText(context, packageState.op))
                else -> Unit
            }
        }
    }

    // Toasts for home card image/sticker failures (unreadable image, sticker limit).
    LaunchedEffect(Unit) {
        viewModel.homeImageEvents.collect { res -> toast.show(context.getString(res)) }
    }

    val homeCardImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::pickHomeCardImage) }
    val homeCardStickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::addHomeCardSticker) }
    val backgroundImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val staged = viewModel.stageBackgroundCrop(uri)
                if (staged != null) {
                    navigator.push(Route.BackgroundCrop(staged))
                } else {
                    toast.show(context.getString(R.string.background_crop_failed))
                }
            }
        }
    }
    // The icon picker is shared by the four slots; the pending slot key routes the result.
    var pendingBottomBarIconSlot by rememberSaveable { mutableStateOf<String?>(null) }
    val bottomBarIconLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val slot = pendingBottomBarIconSlot
        if (uri != null && slot != null) viewModel.pickBottomBarIcon(slot, uri)
        pendingBottomBarIconSlot = null
    }

    val actions = ColorPaletteScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSetThemeMode = viewModel::setThemeMode,
        onSetMiuixMonet = viewModel::setMiuixMonet,
        onSetKeyColor = viewModel::setKeyColor,
        onSetColorMode = viewModel::setColorMode,
        onSetColorStyle = viewModel::setColorStyle,
        onSetColorSpec = viewModel::setColorSpec,
        onSetEnableBlur = viewModel::setEnableBlur,
        onSetEnableProgressiveBlur = viewModel::setEnableProgressiveBlur,
        onSetEnableFloatingBottomBar = viewModel::setEnableFloatingBottomBar,
        onSetEnableFloatingBottomBarBlur = viewModel::setEnableFloatingBottomBarBlur,
        onSetEnableNavigationBadge = viewModel::setEnableNavigationBadge,
        onSetEnablePredictiveBack = {
            viewModel.setEnablePredictiveBack(it)
            AeFoxApp.setEnableOnBackInvokedCallback(context.applicationInfo, it)
            activity?.recreate()
        },
        onSetPageScale = viewModel::setPageScale,
        onSetEnableSwipeDismiss = viewModel::setEnableSwipeDismiss,
        onSetPagerInterceptionMode = viewModel::setPagerInterceptionMode,
        onSetHomeLayout = viewModel::setHomeLayout,
        onSetHomeStatusAlignment = viewModel::setHomeStatusAlignment,
        onSetHomeStatusScale = viewModel::setHomeStatusScale,
        onSetHomePillScale = viewModel::setHomePillScale,
        onSetHomeStatusWidthFraction = viewModel::setHomeStatusWidthFraction,
        onSetHideHomeCardStatusText = viewModel::setHideHomeCardStatusText,
        onSetHideHomeCardDecoration = viewModel::setHideHomeCardDecoration,
        onSetHomeCardImageStyle = viewModel::setHomeCardImageStyle,
        onSetShowHomeCardImageWithoutBackground = viewModel::setShowHomeCardImageWithoutBackground,
        onPickHomeCardImage = {
            homeCardImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onClearHomeCardImage = viewModel::clearHomeCardImage,
        onPickHomeCardSticker = {
            homeCardStickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemoveHomeCardSticker = viewModel::removeHomeCardSticker,
        onUpdateHomeCardStickerTransform = viewModel::updateHomeCardStickerTransform,
        onPickBackgroundImage = {
            backgroundImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onCropCurrentBackground = {
            scope.launch {
                val staged = viewModel.stageCurrentBackgroundCrop()
                if (staged != null) {
                    navigator.push(Route.BackgroundCrop(staged))
                } else {
                    toast.show(context.getString(R.string.background_crop_failed))
                }
            }
        },
        onClearBackgroundImage = viewModel::clearBackgroundImage,
        onSetUseSystemWallpaper = viewModel::setUseSystemWallpaper,
        onSetExtractColorFromBackground = viewModel::setExtractColorFromBackground,
        onSetCardBackgroundAlphaPercent = viewModel::setCardBackgroundAlphaPercent,
        onSetCustomBackgroundBlurPercent = viewModel::setCustomBackgroundBlurPercent,
        onSetCustomBackgroundScrimPercent = viewModel::setCustomBackgroundScrimPercent,
        onPickBottomBarIcon = { slot ->
            pendingBottomBarIconSlot = slot
            bottomBarIconLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onClearBottomBarIcon = viewModel::clearBottomBarIcon,
        onOpenCustomTexts = { navigator.push(Route.CustomTexts) },
        onOpenAppearanceOverlay = onOpenAppearanceOverlay,
        onSetModuleDescriptionMaxLines = viewModel::setModuleDescriptionMaxLines,
        onResetBackgroundAppearance = viewModel::resetBackgroundAppearance,
        onClearAllAppearance = viewModel::clearAllAppearance,
        onImportTheme = {
            importLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*"))
        },
        onExportTheme = {
            showExportOptions = true
        },
        onRestoreDefault = { viewModel.requestRestoreDefault() },
        onCancelTrial = { viewModel.cancelTrial(activity) },
        onOpenAppIcon = { navigator.push(Route.AppIcon) },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> ColorPaletteScreenMiuix(state, actions)
        UiMode.Material -> ColorPaletteScreenMaterial(state, actions)
    }

    pendingImport?.let { imported ->
        ThemeApplyOptionsDialog(
            themeName = pendingImportName,
            imported = imported,
            onConfirm = { options -> viewModel.applyStagedImport(options, activity) },
            onDismiss = viewModel::discardStagedImport,
            title = stringResource(R.string.theme_import_dialog_title),
            message = stringResource(R.string.theme_import_dialog_message),
            confirmLabel = stringResource(R.string.confirm),
        )
    }
    if (showExportOptions) {
        ThemeStoreApplySectionDialog(
            themeName = "",
            onConfirm = { sections ->
                showExportOptions = false
                exportSections = sections
                exportLauncher.launch("aefox_theme.sukisu")
            },
            onDismiss = { showExportOptions = false },
            title = stringResource(R.string.theme_export_dialog_title),
            message = stringResource(R.string.theme_export_dialog_message),
            confirmLabel = stringResource(R.string.theme_export_confirm),
        )
    }
}

/** Copies a SAF Uri into a fixed-name cache file; returns null on failure. */
private fun copyUriToCacheFile(context: android.content.Context, uri: android.net.Uri, name: String): File? =
    runCatching {
        val target = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.takeIf { it.length() > 0L }
    }.getOrNull()

/** Exports the current appearance to cache (suspends until done), then copies into the SAF target Uri. */
private suspend fun exportToUri(
    context: android.content.Context,
    uri: android.net.Uri,
    viewModel: SettingsViewModel,
    adaptive: Boolean,
    sections: Set<ThemeApplySection>,
    show: (String) -> Unit,
) {
    val temp = File(context.cacheDir, "theme_export.sukisu")
    val exported = viewModel.exportNow(temp, adaptive, sections)
    if (exported.isFailure) {
        show(context.getString(R.string.theme_pkg_export_failed))
        temp.delete()
        return
    }
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            temp.inputStream().use { input -> input.copyTo(output) }
        }
        show(context.getString(R.string.theme_pkg_export_saved))
    }.onFailure {
        show(context.getString(R.string.theme_pkg_export_failed))
    }
    temp.delete()
}

/** Toast text for a completed operation. */
private fun packageDoneText(context: android.content.Context, op: ThemePackageOp): String = when (op) {
    ThemePackageOp.EXPORT -> context.getString(R.string.theme_pkg_export_saved)
    ThemePackageOp.TRIAL_START -> context.getString(R.string.theme_pkg_trial_started)
    else -> context.getString(R.string.theme_pkg_apply_done)
}

/** Toast text for a failed operation. */
private fun packageFailedText(context: android.content.Context, op: ThemePackageOp): String = when (op) {
    ThemePackageOp.EXPORT -> context.getString(R.string.theme_pkg_export_failed)
    ThemePackageOp.TRIAL_START -> context.getString(R.string.theme_pkg_trial_failed)
    ThemePackageOp.RESTORE_DEFAULT -> context.getString(R.string.theme_pkg_restore_failed)
    else -> context.getString(R.string.theme_pkg_apply_failed)
}

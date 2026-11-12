package aefox.reyang.ui.screen.appicon

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import aefox.reyang.R
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.component.LocalAeFoxToast
import aefox.reyang.ui.navigation3.LocalNavigator
import aefox.reyang.ui.util.AppIconEntry
import aefox.reyang.ui.util.AppIconPack
import aefox.reyang.ui.util.AppIconPackStorage
import aefox.reyang.ui.util.AppIconShortcut
import aefox.reyang.ui.util.BuiltinAppIcon
import aefox.reyang.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Immutable
data class AppIconUiState(
    val selectedBuiltinId: String,
    val dynamicColor: Boolean = false,
    val packs: List<AppIconPack> = emptyList(),
    val loading: Boolean = true,
    val importing: Boolean = false,
    val pendingDelete: AppIconPack? = null,
)

@Immutable
data class AppIconActions(
    val onBack: () -> Unit,
    val onSelectBuiltin: (BuiltinAppIcon) -> Unit,
    val onSetDynamicColor: (Boolean) -> Unit,
    val onImportPack: () -> Unit,
    val onPinCustomIcon: (AppIconPack, AppIconEntry) -> Unit,
    val onRequestDeletePack: (AppIconPack) -> Unit,
    val onDismissDeletePack: () -> Unit,
    val onConfirmDeletePack: (AppIconPack) -> Unit,
)

/**
 * Launcher icon settings dispatcher.
 *
 * Built-in icons switch the real launcher alias; imported pack icons cannot replace the app
 * icon on Android, so they are pinned to the home screen as shortcuts instead.
 */
@Composable
fun AppIconScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val toast = LocalAeFoxToast.current
    val viewModel = viewModel<SettingsViewModel>()
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var packs by remember { mutableStateOf<List<AppIconPack>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var importing by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<AppIconPack?>(null) }

    LaunchedEffect(Unit) {
        packs = AppIconPackStorage.load(context)
        loading = false
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                try {
                    AppIconPackStorage.import(context, uri)
                        .onSuccess { count ->
                            toast.show(context.getString(R.string.app_icon_import_success, count))
                            packs = AppIconPackStorage.load(context)
                        }
                        .onFailure {
                            toast.show(context.getString(R.string.app_icon_import_failed))
                        }
                } finally {
                    importing = false
                }
            }
        }
    }

    val state = AppIconUiState(
        selectedBuiltinId = settings.appIconId,
        dynamicColor = settings.appIconDynamicColor,
        packs = packs,
        loading = loading,
        importing = importing,
        pendingDelete = pendingDelete,
    )
    val actions = AppIconActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSelectBuiltin = { icon ->
            // Re-applying an unchanged component can restart the process on some ROMs.
            if (icon.id != settings.appIconId) {
                viewModel.setAppIcon(context, icon.id)
                toast.show(context.getString(R.string.app_icon_switch_hint))
            }
        },
        onSetDynamicColor = { viewModel.setAppIconDynamicColor(context, it) },
        onImportPack = {
            if (!importing) {
                importLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/x-zip",
                        "application/x-zip-compressed",
                        "application/octet-stream",
                    ),
                )
            }
        },
        onPinCustomIcon = { pack, entry ->
            scope.launch {
                val bitmap = AppIconPackStorage.loadBitmap(context, pack, entry)
                if (bitmap == null) {
                    toast.show(context.getString(R.string.app_icon_pin_failed))
                } else {
                    val requested = AppIconShortcut.create(
                        context = context,
                        key = "${pack.id}_${entry.id}",
                        label = context.getString(R.string.app_name),
                        bitmap = bitmap,
                    )
                    toast.show(
                        context.getString(
                            if (requested) R.string.app_icon_shortcut_created
                            else R.string.app_icon_shortcut_failed,
                        ),
                    )
                }
            }
        },
        onRequestDeletePack = { pendingDelete = it },
        onDismissDeletePack = { pendingDelete = null },
        onConfirmDeletePack = { pack ->
            pendingDelete = null
            scope.launch {
                AppIconPackStorage.delete(context, pack.id)
                    .onSuccess { packs = AppIconPackStorage.load(context) }
                    .onFailure { toast.show(context.getString(R.string.app_icon_delete_pack_failed)) }
            }
        },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> AppIconScreenMiuix(state, actions)
        UiMode.Material -> AppIconScreenMaterial(state, actions)
    }
}

/** Built-in icon preview: the launcher bitmap rendered at preview size. */
@Composable
fun BuiltinAppIconPreview(
    icon: BuiltinAppIcon,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap = remember(icon.iconRes) { loadDrawableBitmap(context, icon.iconRes) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = stringResource(icon.nameRes),
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

/** Imported pack entry preview (Coil loads the normalized 512 px PNG). */
@Composable
fun CustomAppIconPreview(
    pack: AppIconPack,
    entry: AppIconEntry,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val file = remember(pack.id, entry.fileName) {
        AppIconPackStorage.iconFile(context, pack, entry)
    }
    AsyncImage(
        model = file,
        contentDescription = entry.name,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

private fun loadDrawableBitmap(context: Context, resId: Int): ImageBitmap? = runCatching {
    ContextCompat.getDrawable(context, resId)
        ?.toBitmap(width = 256, height = 256)
        ?.asImageBitmap()
}.getOrNull()

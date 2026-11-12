package aefox.reyang.ui.screen.apps

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import aefox.reyang.apps.SupportedApps
import aefox.reyang.data.repository.SettingsRepositoryImpl
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode

internal data class AppEntry(
    val name: String,
    val packageName: String,
    /** Null when the app is not installed. */
    val versionName: String?,
    val icon: ImageBitmap?,
)

@Composable
fun AppsPager(bottomInnerPadding: Dp) {
    val context = LocalContext.current
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    // Module name sort (theme_module_sort_name_order): ASCENDING / DESCENDING take effect;
    // the manual flag has no manual order data yet, so the default order is shown. The
    // other moduleSort fields are persisted only.
    val sortRepo = remember { SettingsRepositoryImpl() }
    val nameOrder = remember { sortRepo.moduleSortNameOrder }

    // Installed version and icon are install-time facts: resolve them once, not per keystroke.
    val entries = remember(context) {
        val packageManager = context.packageManager
        SupportedApps.all.map { app ->
            // Package visibility (Android 11+) is granted through <queries> in the manifest.
            val detected = app.packages.firstOrNull { installedVersionName(packageManager, it) != null }
            val target = detected ?: app.packages.first()
            AppEntry(
                name = context.getString(app.nameRes),
                packageName = target,
                versionName = detected?.let { installedVersionName(packageManager, it) },
                icon = detected?.let { appIcon(packageManager, it) },
            )
        }
    }
    val sortedEntries = remember(entries, nameOrder) {
        when (nameOrder) {
            "ASCENDING" -> entries.sortedBy { it.name.lowercase() }
            "DESCENDING" -> entries.sortedByDescending { it.name.lowercase() }
            else -> entries
        }
    }
    val matches = remember(query.text, sortedEntries) {
        val keyword = query.text.trim()
        if (keyword.isEmpty()) {
            sortedEntries
        } else {
            sortedEntries.filter {
                it.name.contains(keyword, ignoreCase = true) ||
                    it.packageName.contains(keyword, ignoreCase = true)
            }
        }
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> AppsPagerMiuix(matches, query, { query = it }, bottomInnerPadding)
        UiMode.Material -> AppsPagerMaterial(matches, query, { query = it }, bottomInnerPadding)
    }
}

@Composable
internal fun AppIcon(icon: ImageBitmap?) {
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
    } else {
        // Placeholder keeps the row aligned for apps that are not installed.
        Icon(
            imageVector = Icons.Rounded.Android,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
        )
    }
}

internal fun installedVersionName(packageManager: PackageManager, packageName: String): String? =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull()

/** Count of supported apps installed on this device (navigation badge source). */
internal fun installedSupportedAppCount(context: Context): Int {
    val packageManager = context.packageManager
    return SupportedApps.all.count { app ->
        app.packages.any { installedVersionName(packageManager, it) != null }
    }
}

private fun appIcon(packageManager: PackageManager, packageName: String): ImageBitmap? =
    runCatching {
        val drawable = packageManager.getApplicationInfo(packageName, 0).loadIcon(packageManager)
        // Explicit size: adaptive icons report -1 as intrinsic size and cannot be sized implicitly.
        val bitmap = Bitmap.createBitmap(APP_ICON_PX, APP_ICON_PX, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, APP_ICON_PX, APP_ICON_PX)
        drawable.draw(Canvas(bitmap))
        bitmap.asImageBitmap()
    }.getOrNull()

private const val APP_ICON_PX = 144

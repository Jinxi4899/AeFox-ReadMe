package aefox.reyang.ui.component.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import aefox.reyang.aefoxApp
import aefox.reyang.ui.theme.LocalThemeVisuals
import aefox.reyang.ui.theme.themepackage.BottomBarIconSlot
import aefox.reyang.ui.theme.themepackage.ThemeImageStore
import java.io.File

/**
 * Draws the slot's custom image when present (original colors preserved so
 * authors can ship multicolor icons), otherwise falls back to the built-in
 * [icon] vector (still accepts [tint]).
 */
@Composable
fun BottomBarIconContent(
    icon: ImageVector,
    iconPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    val file = iconPath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile }
    if (file != null) {
        Image(
            painter = rememberAsyncImagePainter(model = file),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit,
        )
    } else {
        Image(
            painter = rememberVectorPainter(icon),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            colorFilter = tint.takeIf { it != Color.Unspecified }?.let { color ->
                ColorFilter.tint(color)
            },
        )
    }
}

/**
 * Reads the active custom bottom bar icon mapping (slot key -> file).
 * Only fixed-name files inside the theme image dir
 * (`theme/bottom_bar_icon_<slot>`) are honored; entries with rewritten paths,
 * invalid slot names or missing files fall back to the built-in vector.
 * Returns an empty map when the theme package carries no icon data.
 */
@Composable
fun rememberBottomBarIconPaths(): Map<String, File> {
    val paths = LocalThemeVisuals.current.bottomBarIcons
    return remember(paths) {
        paths.mapNotNull { (slot, path) ->
            if (BottomBarIconSlot.fromKey(slot) == null) return@mapNotNull null
            val canonical = ThemeImageStore.bottomBarIconFile(aefoxApp, slot)
            if (canonical.isFile && canonical.absolutePath == path) slot to canonical else null
        }.toMap()
    }
}

package aefox.reyang.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Wide-window breakpoint shared by the bottom bar / navigation rail switch and the
 * background-effect device class. Single source: 840dp, or 600dp when the window is
 * close to square (landscape tablet / foldable), mirroring SukiSU's WindowSize helper.
 */
@Composable
fun shouldShowSplitPane(): Boolean {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        val widthDp = windowInfo.containerSize.width.toDp()
        val heightDp = windowInfo.containerSize.height.toDp()
        widthDp >= 840.dp || (widthDp >= 600.dp && heightDp / widthDp < 1.2f)
    }
}

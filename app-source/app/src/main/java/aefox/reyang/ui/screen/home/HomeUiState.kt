package aefox.reyang.ui.screen.home

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle

@Immutable
data class HomeUiState(
    val systemInfo: SystemInfo,
)

@Immutable
data class HomeActions(
    // Pull-to-refresh: re-read permission and activation states.
    val onRefresh: () -> Unit,
)

/**
 * Render geometry constants for the classic status card and the immersive status area
 * (Dp/Sp base values).
 *
 * Data-layer bounds (min/max/defaults) live in ThemePackageStorage.HomeStatusStyle; this
 * only adds the render bases, kept in sync with SukiSU HomeStatusStyle visuals so the
 * home screen, preview and sticker editor align.
 */
object HomeStatusUi {
    val PillMinHeight = 46.dp
    val PillHorizontalPadding = 15.dp
    val PillContentGap = 9.dp
    val PillTouchTarget = 48.dp
    val DotSize = 8.dp
    val DividerWidth = 1.dp
    val DividerHeight = 16.dp
    const val LabelSizeSp = 15f
    const val MetaSizeSp = 11.5f

    val BannerHeight = 88.dp
    val BannerCorner = 20.dp
    val ClusterGap = 10.dp

    /** Classic card height floor (SukiSU's 188dp content floor). */
    val ClassicMinHeight = 188.dp

    /** Image banner height scale (follows the status area scale). */
    fun scaledDp(base: Dp, scale: Float): Dp = base * HomeStatusStyle.coerceScale(scale)

    /** Pill sizes use an independent scale, not the image card scale. */
    fun pillDp(base: Dp, pillScale: Float): Dp = base * HomeStatusStyle.coercePillScale(pillScale)

    fun pillSp(base: Float, pillScale: Float): TextUnit =
        (base * HomeStatusStyle.coercePillScale(pillScale)).sp
}

package aefox.reyang.ui.component.bottombar

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.screen.apps.installedSupportedAppCount
import aefox.reyang.ui.theme.LocalEnableNavigationBadge
import aefox.reyang.ui.util.shouldShowSplitPane
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.utils.springAnimateToPage

class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
    private val animatePageChanges: Boolean,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()

        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                if (animatePageChanges) {
                    pagerState.springAnimateToPage(targetIndex)
                } else {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    animatePageChanges: Boolean = true,
): MainPagerState {
    return remember(pagerState, coroutineScope, animatePageChanges) {
        MainPagerState(pagerState, coroutineScope, animatePageChanges)
    }
}

@Composable
fun useNavigationRail(enableFloatingBottomBar: Boolean): Boolean {
    val wideWindow = shouldShowSplitPane()
    return wideWindow && !(LocalUiMode.current == UiMode.Miuix && enableFloatingBottomBar)
}

/** Counts shown on the navigation bar; no destination carries one today. */
@Immutable
data class NavigationBadgeState(val supportedAppCount: Int = 0)

/**
 * Badge count for a bottom destination (null hides the badge).
 *
 * The tab bar itself carries no counts; the entry point is kept so callers only
 * have one place to read a badge from.
 */
internal fun badgeCountFor(index: Int, state: NavigationBadgeState): Int? = null

/** Badge source: installed supported app count, zero when the user disabled the badge. */
@Composable
internal fun rememberNavigationBadgeState(): NavigationBadgeState {
    val enabled = LocalEnableNavigationBadge.current
    val context = LocalContext.current
    return remember(enabled, context) {
        NavigationBadgeState(if (enabled) installedSupportedAppCount(context) else 0)
    }
}

@Composable
fun BottomBar(
    blurBackdrop: LayerBackdrop?,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> BottomBarMiuix(blurBackdrop, backdrop, modifier)
        UiMode.Material -> BottomBarMaterial()
    }
}

/**
 * Largest font size in [minFontSize]..[maxFontSize] that keeps [text] on a
 * single line inside [maxWidth]. Long custom labels therefore shrink instead of
 * wrapping or squeezing the bar icons; callers still ellipsize at [minFontSize]
 * when even the smallest size does not fit.
 */
@Composable
internal fun rememberSingleLineFontSize(
    text: String,
    maxWidth: Dp,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
): TextUnit {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxWidthPx = if (maxWidth.value.isFinite()) with(density) { maxWidth.roundToPx() } else 0
    val fontScale = density.fontScale
    return remember(text, maxWidthPx, fontScale, maxFontSize, minFontSize, fontWeight, measurer) {
        if (maxWidthPx <= 0) {
            maxFontSize
        } else {
            val min = minFontSize.value
            var size = maxFontSize.value
            while (size > min) {
                val layout = measurer.measure(
                    text = AnnotatedString(text),
                    style = TextStyle(fontSize = size.sp, fontWeight = fontWeight),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    constraints = Constraints(maxWidth = maxWidthPx),
                )
                if (!layout.hasVisualOverflow) break
                size = (size - 0.5f).coerceAtLeast(min)
            }
            size.sp
        }
    }
}

@Composable
fun SideRail(
    modifier: Modifier = Modifier,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> NavigationRailMiuix(modifier)
        UiMode.Material -> NavigationRailMaterial(modifier)
    }
}

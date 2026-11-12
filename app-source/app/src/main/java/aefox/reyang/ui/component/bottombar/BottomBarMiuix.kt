package aefox.reyang.ui.component.bottombar

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aefox.reyang.R
import aefox.reyang.LocalMainPagerState
import aefox.reyang.ui.component.FloatingBottomBar
import aefox.reyang.ui.component.FloatingBottomBarItem
import aefox.reyang.ui.theme.LocalEnableFloatingBottomBar
import aefox.reyang.ui.theme.LocalEnableFloatingBottomBarBlur
import aefox.reyang.ui.theme.LocalEnableProgressiveBlur
import aefox.reyang.ui.theme.SukiSuSuperuserNouns
import aefox.reyang.ui.theme.neutralBarContainerColor
import aefox.reyang.ui.theme.themeCustomLabel
import aefox.reyang.ui.util.BlurredBar
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BadgedBox
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBarMiuix(
    blurBackdrop: LayerBackdrop?,
    backdrop: Backdrop,
    modifier: Modifier,
) {
    val mainState = LocalMainPagerState.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val progressiveBlur = LocalEnableProgressiveBlur.current && isRuntimeShaderSupported()
    val badgeState = rememberNavigationBadgeState()

    val iconPaths = rememberBottomBarIconPaths()
    val items = BottomBarDestination.entries.map { destination ->
        BottomBarItemData(
            label = themeCustomLabel(
                destination.textKey,
                stringResource(destination.label),
                destination.retargetNouns,
                destination.aliasTextKey,
            ),
            icon = destination.icon,
            iconPath = iconPaths[destination.slotKey]?.absolutePath,
        )
    }
    val maxLabelWidth = maxFloatingItemLabelWidth(items.size)
    if (!enableFloatingBottomBar) {
        BlurredBar(blurBackdrop, progressiveGradient = ProgressiveBlur.Bottom) {
            NavigationBar(
                modifier = modifier,
                // Neutral translucent surface: theme packages only recolor icons and labels.
                color = if (blurBackdrop != null) {
                    Color.Transparent
                } else {
                    neutralBarContainerColor().copy(alpha = 0.88f)
                },
                content = {
                    items.forEachIndexed { index, item ->
                        val selected = mainState.selectedPage == index
                        val badgeContent: (@Composable () -> Unit)? =
                            badgeCountFor(index, badgeState)?.let { count ->
                                { Badge { Text(count.toString()) } }
                            }
                        MiuixNavigationBarItem(
                            selected = selected,
                            onClick = { mainState.animateToPage(index) },
                            icon = item.icon,
                            iconPath = item.iconPath,
                            label = item.label,
                            badge = badgeContent,
                        )
                    }
                }
            )
        }
    } else {
        FloatingBottomBar(
            modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            selectedIndex = { mainState.selectedPage },
            onSelected = { mainState.animateToPage(it) },
            backdrop = backdrop,
            tabsCount = items.size,
            isBlurEnabled = enableFloatingBottomBarBlur,
            progressiveBlur = progressiveBlur,
        ) {
            items.forEachIndexed { index, item ->
                FloatingBottomBarItem(
                    onClick = {
                        mainState.animateToPage(index)
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 76.dp)
                ) {
                    val badgeCount = badgeCountFor(index, badgeState)
                    // Content color follows the provider: onSurface in the base row, accent in
                    // the floating bar's accent copy so the selected tab recolors inside the pill.
                    val itemContentColor = MiuixLocalContentColor.current
                    val iconContent: @Composable () -> Unit = {
                        BottomBarIconContent(
                            icon = item.icon,
                            iconPath = item.iconPath,
                            contentDescription = item.label,
                            tint = itemContentColor,
                        )
                    }
                    if (badgeCount != null) {
                        BadgedBox(badge = { Badge { Text(badgeCount.toString()) } }) { iconContent() }
                    } else {
                        iconContent()
                    }
                    Text(
                        text = item.label,
                        fontSize = rememberSingleLineFontSize(
                            text = item.label,
                            maxWidth = maxLabelWidth,
                            maxFontSize = 11.sp,
                            minFontSize = 8.sp,
                        ),
                        lineHeight = 14.sp,
                        color = itemContentColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Per-tab label width available to the floating pill: the pill may grow to the
 * screen width, so long labels are fitted against roughly one screen share and
 * never hide the fourth entry.
 */
@Composable
private fun maxFloatingItemLabelWidth(tabsCount: Int): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    return (screenWidth - 16.dp) / tabsCount.coerceAtLeast(1) - 6.dp
}

/** Render data for a bottom bar item: label has custom text applied, iconPath falls back to the built-in vector when empty. */
private data class BottomBarItemData(
    val label: String,
    val icon: ImageVector,
    val iconPath: String?,
)

/**
 * Item mirroring the miuix NavigationBarItem layout. It is used for every
 * destination so the label can be fitted to the item width instead of
 * wrapping, and it also hosts custom theme-package icon images.
 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.MiuixNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    iconPath: String?,
    label: String,
    badge: (@Composable () -> Unit)?,
) {
    val tint = MiuixTheme.colorScheme.onSurfaceContainer.copy(
        alpha = if (selected) 1f else NavigationBarDefaults.UnselectedAlpha,
    )
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .height(NavigationBarDefaults.ItemHeight)
            .weight(1f)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(modifier = Modifier.padding(top = NavigationBarDefaults.IconTopPadding)) {
            if (badge != null) {
                BadgedBox(badge = { badge() }) {
                    BottomBarIconContent(
                        icon = icon,
                        iconPath = iconPath,
                        contentDescription = null,
                        tint = tint,
                        size = NavigationBarDefaults.IconSize,
                    )
                }
            } else {
                BottomBarIconContent(
                    icon = icon,
                    iconPath = iconPath,
                    contentDescription = null,
                    tint = tint,
                    size = NavigationBarDefaults.IconSize,
                )
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp, bottom = NavigationBarDefaults.BottomPadding),
        ) {
            Text(
                text = label,
                color = tint,
                fontSize = rememberSingleLineFontSize(
                    text = label,
                    maxWidth = maxWidth,
                    maxFontSize = NavigationBarDefaults.LabelFontSize,
                    minFontSize = 8.sp,
                    fontWeight = fontWeight,
                ),
                fontWeight = fontWeight,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Bottom bar entries for the four home pages.
 *
 * [slotKey] aligns with the theme package's tab-icon slot names (Home->home,
 * Apps (app list)->superuser, Setting->setting). [textKey] is a label key that a
 * theme package may override through custom text, matching the tab labels its
 * authoring tool uses.
 */
enum class BottomBarDestination(
    @get:StringRes val label: Int,
    val icon: ImageVector,
    val slotKey: String,
    val textKey: String?,
    val aliasTextKey: String? = null,
    /** SukiSU page nouns the package may have renamed; replaced with our own label. */
    val retargetNouns: List<String> = emptyList(),
) {
    Home(R.string.home, Icons.Rounded.Cottage, "home", "page.home"),
    Apps(
        R.string.apps,
        Icons.Rounded.Apps,
        "superuser",
        "page.superuser",
        retargetNouns = SukiSuSuperuserNouns,
    ),
    Setting(R.string.settings, Icons.Rounded.Settings, "setting", "page.settings"),
}

package aefox.reyang.ui.component.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aefox.reyang.R
import aefox.reyang.data.repository.SettingsRepositoryImpl
import aefox.reyang.LocalMainPagerState
import aefox.reyang.ui.theme.neutralBarContainerColor
import aefox.reyang.ui.theme.themeCustomLabel
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BadgedBox
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun NavigationRailMiuix(
    modifier: Modifier = Modifier,
) {
    val mainState = LocalMainPagerState.current
    val iconPaths = rememberBottomBarIconPaths()
    val badgeState = rememberNavigationBadgeState()

    val settingsRepo = remember { SettingsRepositoryImpl() }
    val state = rememberNavigationRailState(
        initialValue = if (settingsRepo.navigationRailExpanded) {
            NavigationRailValue.Expanded
        } else {
            NavigationRailValue.Collapsed
        },
    )
    LaunchedEffect(state.currentValue) {
        settingsRepo.navigationRailExpanded = state.isExpanded
    }
    val expanded = state.isExpanded

    NavigationRail(
        modifier = modifier,
        state = state,
        // Neutral translucent surface: theme packages only recolor icons and labels.
        color = neutralBarContainerColor().copy(alpha = 0.88f),
        expandContentDescription = stringResource(R.string.nav_rail_expand),
        collapseContentDescription = stringResource(R.string.nav_rail_collapse),
    ) {
        BottomBarDestination.entries.forEachIndexed { index, destination ->
            val selected = mainState.selectedPage == index
            val label = themeCustomLabel(
                destination.textKey,
                stringResource(destination.label),
                destination.retargetNouns,
                destination.aliasTextKey,
            )
            val iconPath = iconPaths[destination.slotKey]?.absolutePath
            // A self-drawn item is used for every destination so long custom
            // labels can be fitted instead of widening or wrapping.
            MiuixRailItem(
                selected = selected,
                onClick = { mainState.animateToPage(index) },
                icon = destination.icon,
                iconPath = iconPath,
                label = label,
                expanded = expanded,
                badgeCount = badgeCountFor(index, badgeState),
            )
        }
    }
}

/** Rail item: icon only when collapsed, icon + fitted text when expanded (matches the miuix rail look). */
@Composable
private fun MiuixRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconPath: String?,
    label: String,
    expanded: Boolean,
    badgeCount: Int?,
) {
    val tint = MiuixTheme.colorScheme.onSurfaceContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) {
                    // Neutral indicator, mirrors the miuix rail item.
                    MiuixTheme.colorScheme.surfaceContainerHigh
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (expanded) {
            Arrangement.spacedBy(10.dp)
        } else {
            Arrangement.Center
        },
    ) {
        val iconContent: @Composable () -> Unit = {
            BottomBarIconContent(
                icon = icon,
                iconPath = iconPath,
                contentDescription = label,
                tint = tint,
            )
        }
        if (badgeCount != null) {
            BadgedBox(badge = { Badge { Text(badgeCount.toString()) } }) { iconContent() }
        } else {
            iconContent()
        }
        if (expanded) {
            val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = rememberSingleLineFontSize(
                        text = label,
                        maxWidth = maxWidth,
                        maxFontSize = 13.sp,
                        minFontSize = 9.sp,
                        fontWeight = fontWeight,
                    ),
                    fontWeight = fontWeight,
                    color = tint,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

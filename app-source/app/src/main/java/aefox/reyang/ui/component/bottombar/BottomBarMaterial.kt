package aefox.reyang.ui.component.bottombar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import aefox.reyang.R
import aefox.reyang.LocalMainPagerState
import aefox.reyang.ui.theme.neutralBarContainerColor
import aefox.reyang.ui.theme.themeCustomLabel

@Composable
fun BottomBarMaterial() {
    val mainPagerState = LocalMainPagerState.current
    val iconPaths = rememberBottomBarIconPaths()
    val badgeState = rememberNavigationBadgeState()

    val items = listOf(
        Triple(R.string.home, Icons.Filled.Home, Icons.Outlined.Home),
        Triple(R.string.apps, Icons.Filled.Apps, Icons.Outlined.Apps),
        Triple(R.string.settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    )
    // Neutral item colors: selection only paints a neutral indicator, icons/text keep
    // the surface foreground color instead of the accent color.
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSurface,
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
        indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    FlexibleBottomAppBar(
        // Neutral translucent surface: theme packages only recolor icons and labels.
        containerColor = neutralBarContainerColor().copy(alpha = 0.88f),
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    ) {
        items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val destination = BottomBarDestination.entries[index]
            val selected = mainPagerState.selectedPage == index
            // The custom image shares one file across selected/unselected states (original colors kept, no selected-state tint).
            val iconPath = iconPaths[destination.slotKey]?.absolutePath
            val text = themeCustomLabel(
                destination.textKey,
                stringResource(label),
                destination.retargetNouns,
                destination.aliasTextKey,
            )
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        mainPagerState.animateToPage(index)
                    }
                },
                icon = {
                    val badgeCount = badgeCountFor(index, badgeState)
                    val tint = LocalContentColor.current
                    val iconContent: @Composable () -> Unit = {
                        BottomBarIconContent(
                            icon = if (selected) selectedIcon else unselectedIcon,
                            iconPath = iconPath,
                            contentDescription = text,
                            tint = tint,
                        )
                    }
                    if (badgeCount != null) {
                        BadgedBox(badge = { Badge { Text(badgeCount.toString()) } }) { iconContent() }
                    } else {
                        iconContent()
                    }
                },
                label = {
                    BoxWithConstraints {
                        Text(
                            text,
                            fontSize = rememberSingleLineFontSize(
                                text = text,
                                maxWidth = maxWidth,
                                maxFontSize = 12.sp,
                                minFontSize = 9.sp,
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = itemColors,
            )
        }
    }
}

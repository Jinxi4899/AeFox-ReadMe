package aefox.reyang.ui.component.bottombar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import aefox.reyang.R
import aefox.reyang.data.repository.SettingsRepositoryImpl
import aefox.reyang.LocalMainPagerState
import aefox.reyang.ui.theme.neutralBarContainerColor
import aefox.reyang.ui.theme.themeCustomLabel

@Composable
fun NavigationRailMaterial(
    modifier: Modifier = Modifier,
) {
    val mainPagerState = LocalMainPagerState.current
    val iconPaths = rememberBottomBarIconPaths()
    val badgeState = rememberNavigationBadgeState()

    val items = listOf(
        Triple(R.string.home, Icons.Filled.Home, Icons.Outlined.Home),
        Triple(R.string.apps, Icons.Filled.Apps, Icons.Outlined.Apps),
        Triple(R.string.settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    val settingsRepo = remember { SettingsRepositoryImpl() }
    val state = rememberWideNavigationRailState(
        initialValue = if (settingsRepo.navigationRailExpanded) {
            WideNavigationRailValue.Expanded
        } else {
            WideNavigationRailValue.Collapsed
        },
    )
    val scope = rememberCoroutineScope()
    val expanded = state.targetValue == WideNavigationRailValue.Expanded
    LaunchedEffect(state.targetValue) {
        settingsRepo.navigationRailExpanded =
            state.targetValue == WideNavigationRailValue.Expanded
    }

    WideNavigationRail(
        modifier = modifier.fillMaxHeight(),
        state = state,
        colors = WideNavigationRailDefaults.colors().copy(
            // Neutral translucent surface: theme packages only recolor icons and labels.
            containerColor = neutralBarContainerColor().copy(alpha = 0.88f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Start + WindowInsetsSides.Vertical
        ),
        contentPadding = PaddingValues(vertical = 20.dp),
        header = {
            IconButton(
                modifier = Modifier.padding(start = 24.dp),
                onClick = {
                    scope.launch {
                        if (expanded) state.collapse() else state.expand()
                    }
                },
            ) {
                Icon(
                    if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = stringResource(
                        if (expanded) R.string.nav_rail_collapse else R.string.nav_rail_expand
                    )
                )
            }
        },
    ) {
        items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val destination = BottomBarDestination.entries[index]
            val selected = mainPagerState.selectedPage == index
            val iconPath = iconPaths[destination.slotKey]?.absolutePath
            val text = themeCustomLabel(
                destination.textKey,
                stringResource(label),
                destination.retargetNouns,
                destination.aliasTextKey,
            )
            WideNavigationRailItem(
                railExpanded = expanded,
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
                colors = WideNavigationRailItemDefaults.colors(
                    // Selection only paints a neutral indicator; no accent-colored icon/text.
                    selectedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColorTopIconPosition = MaterialTheme.colorScheme.onSurface,
                    selectedTextColorStartIconPosition = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

package aefox.reyang.ui.screen.appearanceoverlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aefox.reyang.R
import aefox.reyang.ui.component.material.LocalListItemShapes
import aefox.reyang.ui.component.material.SegmentedColumn
import aefox.reyang.ui.component.material.SegmentedListItem
import aefox.reyang.ui.component.material.expressiveTopAppBarColors
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.themepackage.AppearanceColorMode
import aefox.reyang.ui.theme.themepackage.AppearanceColorToken
import aefox.reyang.ui.theme.themepackage.AppearanceOverlayData
import aefox.reyang.ui.theme.themepackage.AppearanceOverlayStore
import aefox.reyang.ui.theme.themepackage.colorTokenValue
import aefox.reyang.ui.theme.themepackage.effectiveColorToken

/** Static color overlay editor (Material expressive mode). */
@Composable
fun AppearanceOverlayScreenMaterial(
    data: AppearanceOverlayData,
    actions: AppearanceOverlayActions,
) {
    var modeIndex by rememberSaveable { mutableIntStateOf(AppearanceColorMode.SHARED.ordinal) }
    val mode = AppearanceColorMode.entries[modeIndex]

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appearance_overlay_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = actions.onReset) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            contentDescription = stringResource(R.string.appearance_overlay_reset),
                            tint = Color(0xFFE53935),
                        )
                    }
                },
                colors = expressiveTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                val content: List<@Composable () -> Unit> = listOf(
                    {
                        OverlayContainer {
                            Text(
                                text = stringResource(R.string.appearance_page_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 14.dp,
                                ),
                            )
                        }
                    },
                )
                SegmentedColumn(content = content)
            }

            item {
                val content: List<@Composable () -> Unit> = buildList {
                    add {
                        OverlaySectionHeader(
                            title = stringResource(R.string.appearance_color_mode),
                            summary = stringResource(
                                R.string.appearance_color_mode_summary,
                            ),
                            icon = Icons.Rounded.Colorize,
                        )
                    }
                    add {
                        OverlayContainer {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                AppearanceColorMode.entries.forEachIndexed { index, entry ->
                                    SegmentedButton(
                                        selected = modeIndex == index,
                                        onClick = { modeIndex = index },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = AppearanceColorMode.entries.size,
                                        ),
                                        label = {
                                            Text(stringResource(entry.titleRes()))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                SegmentedColumn(content = content)
            }

            item {
                val content: List<@Composable () -> Unit> = buildList {
                    add {
                        OverlaySectionHeader(
                            title = stringResource(R.string.appearance_section_text_colors),
                            summary = stringResource(
                                R.string.appearance_section_text_colors_summary,
                            ),
                            icon = Icons.Rounded.Colorize,
                        )
                    }
                    listOf(
                        AppearanceColorToken.TEXT_PRIMARY,
                        AppearanceColorToken.TEXT_SECONDARY,
                        AppearanceColorToken.TEXT_DISABLED,
                        AppearanceColorToken.ON_ACCENT,
                    ).forEach { token ->
                        add {
                            OverlayColorRowMaterial(
                                data = data,
                                mode = mode,
                                token = token,
                                actions = actions,
                            )
                        }
                    }
                }
                SegmentedColumn(content = content)
            }

            item {
                val content: List<@Composable () -> Unit> = buildList {
                    add {
                        OverlaySectionHeader(
                            title = stringResource(R.string.appearance_section_palette),
                            summary = stringResource(
                                R.string.appearance_section_palette_summary,
                            ),
                            icon = Icons.Rounded.Palette,
                        )
                    }
                    listOf(
                        AppearanceColorToken.ACCENT,
                        AppearanceColorToken.SURFACE,
                        AppearanceColorToken.CARD,
                        AppearanceColorToken.OUTLINE,
                        AppearanceColorToken.ERROR,
                    ).forEach { token ->
                        add {
                            OverlayColorRowMaterial(
                                data = data,
                                mode = mode,
                                token = token,
                                actions = actions,
                            )
                        }
                    }
                }
                SegmentedColumn(content = content)
            }

            item {
                val content: List<@Composable () -> Unit> = listOf(
                    {
                        OverlayContainer {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                TextButton(
                                    onClick = actions.onReset,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(stringResource(R.string.appearance_overlay_reset))
                                }
                                FilledTonalButton(
                                    onClick = actions.onApply,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(stringResource(R.string.appearance_overlay_apply))
                                }
                            }
                        }
                    },
                )
                SegmentedColumn(content = content)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/** Segmented list container with the shared rounded shape and opaque surface. */
@Composable
private fun OverlayContainer(content: @Composable () -> Unit) {
    val shape = LocalListItemShapes.current?.shape ?: RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    ) {
        content()
    }
}

@Composable
private fun OverlaySectionHeader(
    title: String,
    summary: String,
    icon: ImageVector,
) {
    SegmentedListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        leadingContent = { OverlayIconTile(icon, title) },
    )
}

@Composable
private fun OverlayIconTile(icon: ImageVector, contentDescription: String?) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun OverlayColorRowMaterial(
    data: AppearanceOverlayData,
    mode: AppearanceColorMode,
    token: AppearanceColorToken,
    actions: AppearanceOverlayActions,
) {
    val explicit = data.colorTokenValue(mode, token)
    val effective = data.effectiveColorToken(mode, token)
    val summary = when {
        explicit != null -> explicit
        mode != AppearanceColorMode.SHARED && effective != null ->
            stringResource(R.string.appearance_shared_value, effective.second)
        else -> stringResource(R.string.appearance_follow_theme)
    }
    SegmentedListItem(
        onClick = { actions.onPickColor(OverlayColorTarget(mode, token)) },
        headlineContent = { Text(stringResource(token.titleRes())) },
        supportingContent = { Text(summary) },
        leadingContent = {
            OverlayColorSwatch(
                color = effective?.second
                    ?.let(AppearanceOverlayStore::parseColorValue)
                    ?.let { Color(it) },
                outlineColor = MaterialTheme.colorScheme.outline,
                emptyColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        },
        trailingContent = if (explicit != null) {
            {
                IconButton(
                    onClick = {
                        actions.onClearOverride(OverlayColorTarget(mode, token))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = stringResource(R.string.appearance_override_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            null
        },
    )
}

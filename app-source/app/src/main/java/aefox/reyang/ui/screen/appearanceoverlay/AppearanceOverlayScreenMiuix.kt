package aefox.reyang.ui.screen.appearanceoverlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aefox.reyang.R
import aefox.reyang.ui.theme.LocalEnableBlur
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import aefox.reyang.ui.theme.themepackage.AppearanceColorMode
import aefox.reyang.ui.theme.themepackage.AppearanceColorToken
import aefox.reyang.ui.theme.themepackage.AppearanceOverlayData
import aefox.reyang.ui.theme.themepackage.colorTokenValue
import aefox.reyang.ui.theme.themepackage.effectiveColorToken
import aefox.reyang.ui.util.BlurredBar
import aefox.reyang.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** Static color overlay editor (Miuix mode). */
@Composable
fun AppearanceOverlayScreenMiuix(
    data: AppearanceOverlayData,
    actions: AppearanceOverlayActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(LocalEnableBlur.current)
    val barColor = if (backdrop != null || rememberThemeBackgroundActive()) {
        Color.Transparent
    } else {
        colorScheme.surface
    }
    var modeIndex by rememberSaveable { mutableIntStateOf(AppearanceColorMode.SHARED.ordinal) }
    val mode = AppearanceColorMode.entries[modeIndex]

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.appearance_overlay_title),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
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
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.appearance_page_summary),
                        fontSize = 13.sp,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = stringResource(R.string.appearance_color_mode),
                        summary = stringResource(R.string.appearance_color_mode_summary),
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Colorize,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = stringResource(R.string.appearance_color_mode),
                                tint = colorScheme.onBackground,
                            )
                        },
                    )
                    HorizontalDivider()
                    TabRowWithContour(
                        tabs = AppearanceColorMode.entries.map { stringResource(it.titleRes()) },
                        selectedTabIndex = modeIndex,
                        onTabSelected = { modeIndex = it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        colors = TabRowDefaults.tabRowColors(
                            backgroundColor = colorScheme.surfaceContainerHighest,
                            selectedBackgroundColor = colorScheme.surfaceContainer,
                        ),
                    )
                }
            }

            item {
                OverlayCard(
                    title = stringResource(R.string.appearance_section_text_colors),
                    summary = stringResource(R.string.appearance_section_text_colors_summary),
                    icon = Icons.Rounded.Colorize,
                ) {
                    listOf(
                        AppearanceColorToken.TEXT_PRIMARY,
                        AppearanceColorToken.TEXT_SECONDARY,
                        AppearanceColorToken.TEXT_DISABLED,
                        AppearanceColorToken.ON_ACCENT,
                    ).forEachIndexed { index, token ->
                        if (index > 0) HorizontalDivider()
                        OverlayColorRow(
                            data = data,
                            mode = mode,
                            token = token,
                            actions = actions,
                        )
                    }
                }
            }

            item {
                OverlayCard(
                    title = stringResource(R.string.appearance_section_palette),
                    summary = stringResource(R.string.appearance_section_palette_summary),
                    icon = Icons.Rounded.Palette,
                ) {
                    listOf(
                        AppearanceColorToken.ACCENT,
                        AppearanceColorToken.SURFACE,
                        AppearanceColorToken.CARD,
                        AppearanceColorToken.OUTLINE,
                        AppearanceColorToken.ERROR,
                    ).forEachIndexed { index, token ->
                        if (index > 0) HorizontalDivider()
                        OverlayColorRow(
                            data = data,
                            mode = mode,
                            token = token,
                            actions = actions,
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(
                            text = stringResource(R.string.appearance_overlay_reset),
                            onClick = actions.onReset,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = stringResource(R.string.appearance_overlay_apply),
                            onClick = actions.onApply,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun OverlayCard(
    title: String,
    summary: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = summary,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        HorizontalDivider()
        Column(content = content)
    }
}

@Composable
private fun OverlayColorRow(
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
    BasicComponent(
        title = stringResource(token.titleRes()),
        summary = summary,
        startAction = {
            OverlayColorSwatch(
                color = effective?.second
                    ?.let(aefox.reyang.ui.theme.themepackage.AppearanceOverlayStore::parseColorValue)
                    ?.let { Color(it) },
                outlineColor = colorScheme.outline,
                emptyColor = colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(end = 10.dp),
            )
        },
        endActions = {
            if (explicit != null) {
                IconButton(
                    onClick = {
                        actions.onClearOverride(OverlayColorTarget(mode, token))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = stringResource(R.string.appearance_override_clear),
                        tint = colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        },
        onClick = { actions.onPickColor(OverlayColorTarget(mode, token)) },
    )
}

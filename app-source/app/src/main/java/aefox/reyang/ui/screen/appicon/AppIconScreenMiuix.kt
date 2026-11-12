package aefox.reyang.ui.screen.appicon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aefox.reyang.R
import aefox.reyang.ui.theme.LocalEnableBlur
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import aefox.reyang.ui.util.AppIconEntry
import aefox.reyang.ui.util.AppIconPack
import aefox.reyang.ui.util.BlurredBar
import aefox.reyang.ui.util.BuiltinAppIcon
import aefox.reyang.ui.util.BuiltinAppIcons
import aefox.reyang.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowDialog

/** Launcher icon settings (Miuix mode). */
@Composable
fun AppIconScreenMiuix(
    state: AppIconUiState,
    actions: AppIconActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(LocalEnableBlur.current)
    val barColor = if (backdrop != null || rememberThemeBackgroundActive()) {
        Color.Transparent
    } else {
        colorScheme.surface
    }

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = aefox.reyang.ui.theme.themeCustomText(
                        "page.app_icon",
                        stringResource(R.string.app_icon_title),
                    ),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
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
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { Spacer(Modifier.height(12.dp)) }
            item {
                Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SwitchPreference(
                        title = stringResource(R.string.app_icon_dynamic_color_title),
                        summary = stringResource(R.string.app_icon_dynamic_color_summary),
                        checked = state.dynamicColor,
                        onCheckedChange = actions.onSetDynamicColor,
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(
                            text = stringResource(R.string.app_icon_builtin_section),
                            style = MiuixTheme.textStyles.title4,
                            color = colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(12.dp))
                        IconGrid(BuiltinAppIcons.all) { icon ->
                            MiuixIconTile(
                                name = stringResource(icon.nameRes),
                                selected = icon.id == state.selectedBuiltinId,
                                onClick = { actions.onSelectBuiltin(icon) },
                            ) {
                                BuiltinAppIconPreview(icon = icon, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                    BasicComponent(
                        title = stringResource(R.string.app_icon_import_title),
                        summary = stringResource(
                            if (state.importing) R.string.app_icon_importing
                            else R.string.app_icon_import_summary,
                        ),
                        startAction = {
                            if (state.importing) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(20.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.FileUpload,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = null,
                                    tint = colorScheme.onBackground,
                                )
                            }
                        },
                        onClick = {
                            if (!state.importing) actions.onImportPack()
                        },
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
            when {
                state.loading -> item { LoadingCard() }
                state.packs.isEmpty() -> item { EmptyCustomIconsCard() }
                else -> items(state.packs, key = { it.id }) { pack ->
                    CustomIconPackCard(
                        pack = pack,
                        onPin = actions.onPinCustomIcon,
                        onDelete = actions.onRequestDeletePack,
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            item {
                Text(
                    text = stringResource(R.string.app_icon_custom_hint),
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }

    state.pendingDelete?.let { pack ->
        WindowDialog(
            show = true,
            title = stringResource(R.string.app_icon_delete_pack_title),
            summary = stringResource(R.string.app_icon_delete_pack_message, pack.name),
            backgroundColor = colorScheme.surface,
            onDismissRequest = actions.onDismissDeletePack,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    TextButton(
                        text = stringResource(R.string.theme_pkg_cancel),
                        onClick = actions.onDismissDeletePack,
                    )
                    TextButton(
                        text = stringResource(R.string.app_icon_delete_pack_confirm),
                        onClick = { actions.onConfirmDeletePack(pack) },
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            },
        )
    }
}

@Composable
private fun LoadingCard() {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun EmptyCustomIconsCard() {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_icon_empty_custom),
                style = MiuixTheme.textStyles.title4,
                color = colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_icon_empty_custom_summary),
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun CustomIconPackCard(
    pack: AppIconPack,
    onPin: (AppIconPack, AppIconEntry) -> Unit,
    onDelete: (AppIconPack) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pack.name,
                    style = MiuixTheme.textStyles.title4,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onDelete(pack) }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.app_icon_delete_pack_title),
                        tint = colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            IconGrid(pack.icons) { entry ->
                MiuixIconTile(
                    name = entry.name,
                    selected = false,
                    onClick = { onPin(pack, entry) },
                    role = Role.Button,
                    badge = {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.AddToHomeScreen,
                                contentDescription = stringResource(R.string.app_icon_pin_badge),
                                modifier = Modifier.size(11.dp),
                                tint = colorScheme.onSecondaryContainer,
                            )
                        }
                    },
                ) {
                    CustomAppIconPreview(pack = pack, entry = entry, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/** Four-column icon grid; incomplete rows are padded with equal-weight spacers. */
@Composable
private fun <T> IconGrid(items: List<T>, content: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) { content(item) }
                }
                repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MiuixIconTile(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    role: Role = Role.RadioButton,
    badge: (@Composable BoxScope.() -> Unit)? = null,
    preview: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(role = role) {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(shape)
                .background(colorScheme.surfaceVariant)
                .then(
                    if (selected) {
                        Modifier.border(2.dp, colorScheme.primary, shape)
                    } else {
                        Modifier
                    },
                ),
        ) {
            preview()
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.app_icon_selected),
                        modifier = Modifier.size(11.dp),
                        tint = colorScheme.onPrimary,
                    )
                }
            } else {
                badge?.invoke(this)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariantSummary,
        )
    }
}

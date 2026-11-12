package aefox.reyang.ui.screen.customtext

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import aefox.reyang.R
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.component.material.ExpressiveScaffold
import aefox.reyang.ui.component.material.SegmentedColumn
import aefox.reyang.ui.component.material.SegmentedListItem
import aefox.reyang.ui.component.material.TopBarBackButton
import aefox.reyang.ui.component.material.expressiveTopAppBarColors
import aefox.reyang.ui.navigation3.LocalNavigator
import aefox.reyang.ui.screen.colorpalette.MaterialIconTile
import aefox.reyang.ui.screen.colorpalette.MaterialTextFieldRow
import aefox.reyang.ui.screen.colorpalette.MiuixTextFieldRow
import aefox.reyang.ui.screen.settings.SettingsUiState
import aefox.reyang.ui.theme.LocalEnableBlur
import aefox.reyang.ui.theme.SukiSuSuperuserNouns
import aefox.reyang.ui.theme.formatCustomText
import aefox.reyang.ui.theme.joinCustomTextTemplate
import aefox.reyang.ui.theme.splitCustomTextTemplate
import aefox.reyang.ui.theme.retargetTabLabel
import aefox.reyang.ui.theme.themeCustomText
import aefox.reyang.ui.theme.themepackage.CustomTextGroup
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import aefox.reyang.ui.util.BlurredBar
import aefox.reyang.ui.util.rememberBlurBackdrop
import aefox.reyang.ui.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** One editable interface text; [key] null means the home top bar title. */
private data class CustomTextEntry(
    val key: String?,
    @StringRes val nameRes: Int,
    @StringRes val defaultRes: Int,
    val group: CustomTextGroup,
    /** Keys a SukiSU theme package may use for the same slot (read-only fallback). */
    val aliasKeys: List<String> = emptyList(),
    /** SukiSU page nouns replaced with our own label before display. */
    val retargetNouns: List<String> = emptyList(),
    /** Runtime placeholders the user edits around (e.g. the update version code). */
    val formatArgs: List<CustomTextArg> = emptyList(),
)

/** Runtime values substituted into a custom text template. */
private enum class CustomTextArg(@StringRes val labelRes: Int) {
    VERSION_CODE(R.string.custom_text_arg_version_code),
}

/** The custom text keys AeFox consumes (mirrors SukiSU CustomTextKey subsets). */
private val CustomTextEntries = listOf(
    CustomTextEntry(null, R.string.settings_home_title, R.string.app_name, CustomTextGroup.HOME),
    CustomTextEntry(
        "home.status_working",
        R.string.module_status_active,
        R.string.module_status_active,
        CustomTextGroup.HOME,
    ),
    CustomTextEntry(
        "home.info_device_model",
        R.string.home_device_model,
        R.string.home_device_model,
        CustomTextGroup.HOME,
    ),
    CustomTextEntry("page.home", R.string.home, R.string.home, CustomTextGroup.PAGE_MAIN),
    // SukiSU's "superuser" tab maps to our 应用 page, so a package authored there
    // keeps its tab label here.
    CustomTextEntry(
        "page.superuser",
        R.string.apps,
        R.string.apps,
        CustomTextGroup.PAGE_MAIN,
        retargetNouns = SukiSuSuperuserNouns,
    ),
    CustomTextEntry("page.settings", R.string.settings, R.string.settings, CustomTextGroup.PAGE_MAIN),
    CustomTextEntry(
        "page.appearance",
        R.string.settings_theme,
        R.string.settings_theme,
        CustomTextGroup.PAGE_FEATURE,
    ),
    CustomTextEntry(
        "page.custom_texts",
        R.string.settings_custom_texts,
        R.string.settings_custom_texts,
        CustomTextGroup.PAGE_FEATURE,
    ),
    CustomTextEntry(
        "page.app_icon",
        R.string.app_icon_title,
        R.string.app_icon_title,
        CustomTextGroup.PAGE_FEATURE,
    ),
    CustomTextEntry("page.about", R.string.about, R.string.about, CustomTextGroup.PAGE_FEATURE),
)

/** Full-page interface text editor (mirrors SukiSU's CustomTextScreen). */
@Composable
fun CustomTextsScreen() {
    val navigator = LocalNavigator.current
    val viewModel: SettingsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingEntry by remember { mutableStateOf<CustomTextEntry?>(null) }
    var showResetAll by rememberSaveable { mutableStateOf(false) }

    val resetGroup: (CustomTextGroup) -> Unit = { group ->
        if (group == CustomTextGroup.HOME) viewModel.setCustomHomeTitle("")
        val keys = CustomTextEntries.filter { it.group == group }.mapNotNull { it.key }.toSet()
        viewModel.setCustomTexts(uiState.themeVisuals.customTexts - keys)
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> CustomTextsMiuix(
            uiState = uiState,
            onBack = dropUnlessResumed { navigator.pop() },
            onItemClick = { editingEntry = it },
            onResetAll = { showResetAll = true },
            onResetGroup = resetGroup,
        )

        UiMode.Material -> CustomTextsMaterial(
            uiState = uiState,
            onBack = dropUnlessResumed { navigator.pop() },
            onItemClick = { editingEntry = it },
            onResetAll = { showResetAll = true },
            onResetGroup = resetGroup,
        )
    }

    CustomTextsOverlays(
        uiState = uiState,
        viewModel = viewModel,
        editingEntry = editingEntry,
        onDismissEdit = { editingEntry = null },
        showResetAll = showResetAll,
        onDismissReset = { showResetAll = false },
        onConfirmReset = {
            showResetAll = false
            viewModel.setCustomHomeTitle("")
            viewModel.setCustomTexts(emptyMap())
        },
    )
}

@Composable
private fun CustomTextsOverlays(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    editingEntry: CustomTextEntry?,
    onDismissEdit: () -> Unit,
    showResetAll: Boolean,
    onDismissReset: () -> Unit,
    onConfirmReset: () -> Unit,
) {
    editingEntry?.let { entry ->
        val current = entry.currentValue(
            uiState.themeVisuals.customHomeTitle,
            uiState.themeVisuals.customTexts,
            stringResource(entry.defaultRes),
        )
        val key = entry.key
        CustomTextEditDialog(
            entry = entry,
            initialValue = current,
            onDismiss = onDismissEdit,
            onConfirm = { value ->
                if (key == null) {
                    viewModel.setCustomHomeTitle(value)
                } else {
                    viewModel.setCustomTexts(uiState.themeVisuals.customTexts.withValue(key, value))
                }
                onDismissEdit()
            },
            onRestoreDefault = {
                if (key == null) {
                    viewModel.setCustomHomeTitle("")
                } else {
                    viewModel.setCustomTexts(uiState.themeVisuals.customTexts - key)
                }
                onDismissEdit()
            },
        )
    }
    if (showResetAll) {
        ResetAllDialog(onDismiss = onDismissReset, onConfirm = onConfirmReset)
    }
}

private fun CustomTextEntry.currentValue(
    customHomeTitle: String,
    customTexts: Map<String, String>,
    defaultText: String,
): String {
    if (key == null) return customHomeTitle
    val raw = customTexts[key]?.takeIf { it.isNotBlank() }
        ?: aliasKeys.firstNotNullOfOrNull { alias -> customTexts[alias]?.takeIf { it.isNotBlank() } }
    return if (retargetNouns.isEmpty()) raw.orEmpty() else retargetTabLabel(raw, defaultText, retargetNouns)
}

private fun Map<String, String>.withValue(key: String, value: String): Map<String, String> =
    if (value.isBlank()) this - key else this + (key to value)

@Composable
private fun entrySummary(entry: CustomTextEntry, current: String): String {
    val labels = entry.formatArgs.map { stringResource(it.labelRes) }
    val defaultText = formatCustomText(stringResource(entry.defaultRes), labels)
    return if (current.isBlank()) {
        stringResource(R.string.custom_text_default_value, defaultText)
    } else {
        formatCustomText(current, labels)
    }
}

@Composable
private fun CustomTextsMiuix(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onItemClick: (CustomTextEntry) -> Unit,
    onResetAll: () -> Unit,
    onResetGroup: (CustomTextGroup) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(LocalEnableBlur.current)
    val barColor = if (backdrop != null || rememberThemeBackgroundActive()) Color.Transparent else colorScheme.surface

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            BlurredBar(backdrop) {
                CustomTextsMiuixTopBar(barColor, scrollBehavior, onBack, onResetAll)
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            CustomTextsMiuixList(uiState, onItemClick, onResetGroup, innerPadding)
        }
    }
}

@Composable
private fun CustomTextsMiuixTopBar(
    barColor: Color,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    onBack: () -> Unit,
    onResetAll: () -> Unit,
) {
    MiuixTopAppBar(
        color = barColor,
        title = themeCustomText("page.custom_texts", stringResource(R.string.settings_custom_texts)),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            MiuixIconButton(onClick = onBack) {
                val layoutDirection = LocalLayoutDirection.current
                MiuixIcon(
                    modifier = Modifier.graphicsLayer {
                        if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                    },
                    imageVector = MiuixIcons.Back,
                    contentDescription = null,
                    tint = colorScheme.onBackground,
                )
            }
        },
        actions = {
            MiuixIconButton(onClick = onResetAll) {
                MiuixIcon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = stringResource(R.string.custom_text_restore_all),
                    tint = colorScheme.onBackground,
                )
            }
        },
    )
}

@Composable
private fun CustomTextsMiuixList(
    uiState: SettingsUiState,
    onItemClick: (CustomTextEntry) -> Unit,
    onResetGroup: (CustomTextGroup) -> Unit,
    innerPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .scrollEndHaptic()
            .overScrollVertical()
            .padding(innerPadding)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 12.dp),
        overscrollEffect = null,
    ) {
        CustomTextGroup.entries.forEach { group ->
            val entries = CustomTextEntries.filter { it.group == group }
            if (entries.isEmpty()) return@forEach
            item(key = "title-${group.name}") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiuixText(
                        text = stringResource(group.titleRes),
                        fontSize = 13.sp,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, bottom = 8.dp),
                    )
                    MiuixTextButton(
                        text = stringResource(R.string.custom_text_reset_group),
                        onClick = { onResetGroup(group) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
            item(key = "card-${group.name}") {
                CustomTextGroupCardMiuix(entries, uiState, onItemClick)
            }
        }
        item(key = "bottom-inset") {
            Spacer(
                Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                )
            )
        }
    }
}

@Composable
private fun CustomTextGroupCardMiuix(
    entries: List<CustomTextEntry>,
    uiState: SettingsUiState,
    onItemClick: (CustomTextEntry) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            entries.forEach { entry ->
                val current = entry.currentValue(
                    uiState.themeVisuals.customHomeTitle,
                    uiState.themeVisuals.customTexts,
                    stringResource(entry.defaultRes),
                )
                ArrowPreference(
                    title = stringResource(entry.nameRes),
                    summary = entrySummary(entry, current),
                    onClick = { onItemClick(entry) },
                )
            }
        }
    }
}

@Composable
private fun CustomTextsMaterial(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onItemClick: (CustomTextEntry) -> Unit,
    onResetAll: () -> Unit,
    onResetGroup: (CustomTextGroup) -> Unit,
) {
    val navBars = WindowInsets.navigationBars.asPaddingValues()
    ExpressiveScaffold(
        topBar = { CustomTextsMaterialTopBar(onBack, onResetAll) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CustomTextGroup.entries.forEach { group ->
                val entries = CustomTextEntries.filter { it.group == group }
                if (entries.isEmpty()) return@forEach
                item(key = "group-${group.name}") {
                    CustomTextGroupListMaterial(group, entries, uiState, onItemClick, onResetGroup)
                }
            }
            item {
                Spacer(Modifier.height(navBars.calculateBottomPadding() + 16.dp))
            }
        }
    }
}

@Composable
private fun CustomTextsMaterialTopBar(onBack: () -> Unit, onResetAll: () -> Unit) {
    TopAppBar(
        title = {
            Text(themeCustomText("page.custom_texts", stringResource(R.string.settings_custom_texts)))
        },
        navigationIcon = { TopBarBackButton(onClick = onBack) },
        actions = {
            IconButton(onClick = onResetAll) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = stringResource(R.string.custom_text_restore_all),
                )
            }
        },
        colors = expressiveTopAppBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    )
}

@Composable
private fun CustomTextGroupListMaterial(
    group: CustomTextGroup,
    entries: List<CustomTextEntry>,
    uiState: SettingsUiState,
    onItemClick: (CustomTextEntry) -> Unit,
    onResetGroup: (CustomTextGroup) -> Unit,
) {
    val rows: List<@Composable () -> Unit> = buildList {
        entries.forEach { entry ->
            add { CustomTextRowMaterial(entry, uiState, onItemClick) }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(group.titleRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            )
            TextButton(onClick = { onResetGroup(group) }) {
                Text(stringResource(R.string.custom_text_reset_group))
            }
        }
        SegmentedColumn(content = rows)
    }
}

@Composable
private fun CustomTextRowMaterial(
    entry: CustomTextEntry,
    uiState: SettingsUiState,
    onItemClick: (CustomTextEntry) -> Unit,
) {
    val current = entry.currentValue(
        uiState.themeVisuals.customHomeTitle,
        uiState.themeVisuals.customTexts,
        stringResource(entry.defaultRes),
    )
    SegmentedListItem(
        onClick = { onItemClick(entry) },
        headlineContent = { Text(stringResource(entry.nameRes)) },
        supportingContent = { Text(entrySummary(entry, current)) },
        leadingContent = { MaterialIconTile(Icons.Rounded.Edit, stringResource(entry.nameRes)) },
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun CustomTextEditDialog(
    entry: CustomTextEntry,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onRestoreDefault: () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> CustomTextEditDialogMiuix(entry, initialValue, onDismiss, onConfirm, onRestoreDefault)
        UiMode.Material -> CustomTextEditDialogMaterial(entry, initialValue, onDismiss, onConfirm, onRestoreDefault)
    }
}

@Composable
private fun CustomTextEditDialogMiuix(
    entry: CustomTextEntry,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onRestoreDefault: () -> Unit,
) {
    val argCount = entry.formatArgs.size
    val defaultText = stringResource(entry.defaultRes)
    var parts by remember(entry, initialValue) {
        mutableStateOf(splitCustomTextTemplate(initialValue.ifBlank { defaultText }, argCount))
    }
    WindowDialog(
        show = true,
        title = stringResource(R.string.custom_text_edit_title) + " - " + stringResource(entry.nameRes),
        backgroundColor = colorScheme.surface,
        onDismissRequest = onDismiss,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (argCount == 0) {
                    MiuixTextFieldRow(stringResource(entry.nameRes), parts.firstOrNull().orEmpty()) {
                        parts = listOf(it)
                    }
                } else {
                    CustomTextTemplateFieldsMiuix(entry, parts) { index, value ->
                        parts = parts.toMutableList().also { it[index] = value }
                    }
                }
                MiuixText(
                    text = stringResource(R.string.custom_text_edit_hint),
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiuixTextButton(
                        text = stringResource(R.string.custom_text_restore_default),
                        onClick = onRestoreDefault,
                        modifier = Modifier.weight(1f),
                    )
                    MiuixTextButton(
                        text = stringResource(R.string.confirm),
                        onClick = { onConfirm(joinCustomTextTemplate(parts, argCount).trim()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        },
    )
}

/** Editable literal pieces around the runtime placeholders (Miuix). */
@Composable
private fun CustomTextTemplateFieldsMiuix(
    entry: CustomTextEntry,
    parts: List<String>,
    onPartChange: (Int, String) -> Unit,
) {
    parts.forEachIndexed { index, part ->
        val label = if (index == 0) {
            stringResource(entry.nameRes)
        } else {
            stringResource(entry.formatArgs[index - 1].labelRes)
        }
        MiuixTextFieldRow(label, part) { onPartChange(index, it) }
        entry.formatArgs.getOrNull(index)?.let { arg ->
            MiuixText(
                text = stringResource(R.string.custom_text_variable_hint, stringResource(arg.labelRes)),
                fontSize = 11.sp,
                color = colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CustomTextEditDialogMaterial(
    entry: CustomTextEntry,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onRestoreDefault: () -> Unit,
) {
    val argCount = entry.formatArgs.size
    val defaultText = stringResource(entry.defaultRes)
    var parts by remember(entry, initialValue) {
        mutableStateOf(splitCustomTextTemplate(initialValue.ifBlank { defaultText }, argCount))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.custom_text_edit_title) + " - " + stringResource(entry.nameRes))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (argCount == 0) {
                    MaterialTextFieldRow(stringResource(entry.nameRes), parts.firstOrNull().orEmpty()) {
                        parts = listOf(it)
                    }
                } else {
                    CustomTextTemplateFieldsMaterial(entry, parts) { index, value ->
                        parts = parts.toMutableList().also { it[index] = value }
                    }
                }
                Text(
                    text = stringResource(R.string.custom_text_edit_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(joinCustomTextTemplate(parts, argCount).trim()) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onRestoreDefault) {
                    Text(stringResource(R.string.custom_text_restore_default))
                }
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            }
        },
    )
}

/** Editable literal pieces around the runtime placeholders (Material). */
@Composable
private fun CustomTextTemplateFieldsMaterial(
    entry: CustomTextEntry,
    parts: List<String>,
    onPartChange: (Int, String) -> Unit,
) {
    parts.forEachIndexed { index, part ->
        val label = if (index == 0) {
            stringResource(entry.nameRes)
        } else {
            stringResource(entry.formatArgs[index - 1].labelRes)
        }
        MaterialTextFieldRow(label, part) { onPartChange(index, it) }
        entry.formatArgs.getOrNull(index)?.let { arg ->
            Text(
                text = stringResource(R.string.custom_text_variable_hint, stringResource(arg.labelRes)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ResetAllDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val title = stringResource(R.string.custom_text_restore_all)
    val message = stringResource(R.string.custom_text_restore_all_confirm)
    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            WindowDialog(
                show = true,
                title = title,
                summary = message,
                backgroundColor = colorScheme.surface,
                onDismissRequest = onDismiss,
                content = {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        MiuixTextButton(
                            text = stringResource(android.R.string.cancel),
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(20.dp))
                        MiuixTextButton(
                            text = stringResource(R.string.custom_text_restore_all),
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                },
            )
        }

        UiMode.Material -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.custom_text_restore_all))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
                },
            )
        }
    }
}

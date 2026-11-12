package aefox.reyang.ui.screen.appearanceoverlay

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import aefox.reyang.R
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.theme.isInDarkTheme
import aefox.reyang.ui.theme.themepackage.AppearanceColorMode
import aefox.reyang.ui.theme.themepackage.AppearanceColorToken
import aefox.reyang.ui.theme.themepackage.AppearanceOverlayData
import aefox.reyang.ui.theme.themepackage.AppearanceOverlayStore
import aefox.reyang.ui.theme.themepackage.colorTokenValue
import aefox.reyang.ui.theme.themepackage.effectiveColorToken
import aefox.reyang.ui.theme.themepackage.toOverlayColorString
import aefox.reyang.ui.theme.themepackage.withColorToken
import aefox.reyang.ui.viewmodel.SettingsViewModel
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Immutable
data class OverlayColorTarget(
    val mode: AppearanceColorMode,
    val token: AppearanceColorToken,
)

data class AppearanceOverlayActions(
    val onBack: () -> Unit,
    /** Persists the edited draft (null-equivalent when empty). */
    val onApply: () -> Unit,
    /** Clears every color override and persists the empty overlay. */
    val onReset: () -> Unit,
    val onPickColor: (OverlayColorTarget) -> Unit,
    val onClearOverride: (OverlayColorTarget) -> Unit,
)

/**
 * Static color editor for the theme package overlay.
 *
 * Edits a local draft; the Apply action persists it through SettingsViewModel,
 * while Reset clears every override. The layout and copy mirror SukiSU's
 * appearance overlay page.
 */
@Composable
fun AppearanceOverlayScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val persisted = remember(uiState.themeAppearanceJson) {
        uiState.themeAppearanceJson.takeIf { it.isNotBlank() }
            ?.let { raw ->
                runCatching { AppearanceOverlayStore.parse(raw).getOrThrow().data }.getOrNull()
            }
            ?: AppearanceOverlayData()
    }
    var draft by remember(persisted) { mutableStateOf(persisted) }
    var pickerTarget by remember { mutableStateOf<OverlayColorTarget?>(null) }
    val isDark = isInDarkTheme()

    val actions = AppearanceOverlayActions(
        onBack = onBack,
        onApply = {
            viewModel.setThemeAppearance(draft.takeUnless { it.isEmptyOverlay() })
        },
        onReset = {
            draft = AppearanceOverlayData()
            viewModel.setThemeAppearance(null)
        },
        onPickColor = { pickerTarget = it },
        onClearOverride = { target ->
            draft = draft.withColorToken(target.mode, target.token, null)
        },
    )

    pickerTarget?.let { target ->
        val effective = draft.effectiveColorToken(target.mode, target.token)
        val defaultArgb = when (target.mode) {
            AppearanceColorMode.LIGHT -> 0xFF000000.toInt()
            AppearanceColorMode.DARK -> 0xFFFFFFFF.toInt()
            AppearanceColorMode.SHARED -> if (isDark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        }
        val initialArgb = effective?.second
            ?.let(AppearanceOverlayStore::parseColorValue)
            ?: defaultArgb
        AppearanceColorPickerDialog(
            title = "${stringResource(target.token.titleRes())} · ${
                stringResource(target.mode.titleRes())
            }",
            initialColor = Color(initialArgb),
            clearable = draft.colorTokenValue(target.mode, target.token) != null,
            onDismissRequest = { pickerTarget = null },
            onConfirm = { color ->
                draft = draft.withColorToken(
                    target.mode,
                    target.token,
                    color.toArgb().toOverlayColorString(),
                )
                pickerTarget = null
            },
            onClear = {
                draft = draft.withColorToken(target.mode, target.token, null)
                pickerTarget = null
            },
        )
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> AppearanceOverlayScreenMiuix(data = draft, actions = actions)
        UiMode.Material -> AppearanceOverlayScreenMaterial(data = draft, actions = actions)
    }
}

private fun AppearanceOverlayData.isEmptyOverlay(): Boolean = colors == null && image == null

fun AppearanceColorToken.titleRes(): Int = when (this) {
    AppearanceColorToken.ACCENT -> R.string.appearance_color_accent
    AppearanceColorToken.ON_ACCENT -> R.string.appearance_color_on_accent
    AppearanceColorToken.TEXT_PRIMARY -> R.string.appearance_color_text_primary
    AppearanceColorToken.TEXT_SECONDARY -> R.string.appearance_color_text_secondary
    AppearanceColorToken.TEXT_DISABLED -> R.string.appearance_color_text_disabled
    AppearanceColorToken.SURFACE -> R.string.appearance_color_surface
    AppearanceColorToken.CARD -> R.string.appearance_color_card
    AppearanceColorToken.OUTLINE -> R.string.appearance_color_outline
    AppearanceColorToken.ERROR -> R.string.appearance_color_error
}

fun AppearanceColorMode.titleRes(): Int = when (this) {
    AppearanceColorMode.SHARED -> R.string.appearance_overlay_section_shared
    AppearanceColorMode.LIGHT -> R.string.appearance_overlay_section_light
    AppearanceColorMode.DARK -> R.string.appearance_overlay_section_dark
}

/** Filled circle for an override; a slashed outline when the token follows the theme. */
@Composable
fun OverlayColorSwatch(
    color: Color?,
    outlineColor: Color,
    emptyColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f - 1.dp.toPx()
        if (color == null) {
            drawCircle(color = emptyColor, radius = radius)
            drawCircle(
                color = outlineColor,
                radius = radius,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawLine(
                color = outlineColor,
                start = androidx.compose.ui.geometry.Offset(radius * 0.35f, radius * 0.35f),
                end = androidx.compose.ui.geometry.Offset(
                    this.size.width - radius * 0.35f,
                    this.size.height - radius * 0.35f,
                ),
                strokeWidth = 1.dp.toPx(),
            )
        } else {
            drawCircle(color = color, radius = radius)
            drawCircle(
                color = outlineColor,
                radius = radius,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

private val HueColors = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
    Color.Red,
)

/** Opaque HSV + hex picker used by both UI modes. */
@Composable
private fun AppearanceColorPickerDialog(
    title: String,
    initialColor: Color,
    clearable: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (Color) -> Unit,
    onClear: () -> Unit,
) {
    val miuix = LocalUiMode.current == UiMode.Miuix
    val initialArgb = initialColor.toArgb()
    val initialHsv = FloatArray(3).also { AndroidColor.colorToHSV(initialArgb, it) }

    var hue by remember(initialArgb) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialArgb) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(initialArgb) { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember(initialArgb) {
        mutableFloatStateOf(AndroidColor.alpha(initialArgb) / 255f)
    }
    var hexText by remember(initialArgb) { mutableStateOf(initialArgb.toOverlayColorString()) }
    var hexFocused by remember { mutableStateOf(false) }
    var hexEditing by remember { mutableStateOf(false) }
    var hexError by remember { mutableStateOf(false) }

    val currentArgb = AndroidColor.HSVToColor(
        (alpha * 255f).roundToInt().coerceIn(0, 255),
        floatArrayOf(hue, saturation, value),
    )
    val currentColor = Color(currentArgb)

    LaunchedEffect(currentArgb, hexFocused, hexEditing) {
        if (!hexFocused && !hexEditing) {
            hexText = currentArgb.toOverlayColorString()
            hexError = false
        }
    }

    fun applyHex(raw: String) {
        val candidate = raw.trim()
        if (candidate.isEmpty()) {
            hexError = false
            return
        }
        val normalized = if (candidate.startsWith("#")) candidate else "#$candidate"
        val parsed = AppearanceOverlayStore.parseColorValue(normalized)
        if (parsed == null) {
            hexError = true
            return
        }
        hexError = false
        val hsv = FloatArray(3).also { AndroidColor.colorToHSV(parsed, it) }
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        alpha = AndroidColor.alpha(parsed) / 255f
    }

    val body = @Composable {
        PickerBody(
            miuix = miuix,
            hue = hue,
            saturation = saturation,
            value = value,
            alpha = alpha,
            currentColor = currentColor,
            hexText = hexText,
            hexError = hexError,
            onHexChange = {
                hexEditing = true
                hexText = it
                applyHex(it)
            },
            onHexFocusChange = {
                hexFocused = it
                if (!it) hexEditing = false
            },
            onHueChange = {
                hexEditing = false
                hue = it
            },
            onSaturationValueChange = { s, v ->
                hexEditing = false
                saturation = s
                value = v
            },
            onAlphaChange = {
                hexEditing = false
                alpha = it
            },
        )
    }

    if (miuix) {
        OverlayDialog(
            show = true,
            title = title,
            backgroundColor = MiuixTheme.colorScheme.surface,
            onDismissRequest = onDismissRequest,
            content = {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    body()
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (clearable) {
                            MiuixTextButton(
                                text = stringResource(R.string.appearance_override_clear),
                                onClick = onClear,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        MiuixTextButton(
                            text = stringResource(android.R.string.cancel),
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                        )
                        MiuixTextButton(
                            text = stringResource(R.string.appearance_picker_apply),
                            onClick = { onConfirm(currentColor) },
                            modifier = Modifier.weight(1f),
                            enabled = !hexError,
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults
                                .textButtonColorsPrimary(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            },
        )
    } else {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    MaterialText(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(16.dp))
                    body()
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (clearable) {
                            MaterialTextButton(onClick = onClear) {
                                MaterialText(stringResource(R.string.appearance_override_clear))
                            }
                        }
                        MaterialTextButton(onClick = onDismissRequest) {
                            MaterialText(stringResource(android.R.string.cancel))
                        }
                        MaterialTextButton(
                            onClick = { onConfirm(currentColor) },
                            enabled = !hexError,
                        ) {
                            MaterialText(stringResource(R.string.appearance_picker_apply))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerBody(
    miuix: Boolean,
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float,
    currentColor: Color,
    hexText: String,
    hexError: Boolean,
    onHexChange: (String) -> Unit,
    onHexFocusChange: (Boolean) -> Unit,
    onHueChange: (Float) -> Unit,
    onSaturationValueChange: (Float, Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
) {
    val onSurface = if (miuix) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val outline = if (miuix) {
        MiuixTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outline
    }
    val thumbColor = if (miuix) {
        MiuixTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val primary = if (miuix) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val errorColor = if (miuix) {
        MiuixTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(currentColor),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                HexColorField(
                    value = hexText,
                    onValueChange = onHexChange,
                    onFocusChange = onHexFocusChange,
                    isError = hexError,
                    miuix = miuix,
                )
                if (hexError) {
                    PickerLabel(
                        text = stringResource(R.string.appearance_picker_hex_invalid),
                        color = errorColor,
                        miuix = miuix,
                    )
                }
                PickerLabel(
                    text = stringResource(
                        R.string.appearance_picker_alpha_value,
                        (alpha * 100f).roundToInt(),
                    ),
                    color = onSurface.copy(alpha = 0.7f),
                    miuix = miuix,
                )
            }
        }

        SaturationValueField(
            hue = hue,
            saturation = saturation,
            value = value,
            outline = outline,
            onChange = onSaturationValueChange,
        )

        GradientBar(
            brush = Brush.horizontalGradient(HueColors),
            fraction = hue / 360f,
            thumbColor = thumbColor,
            outline = outline,
            onChange = { onHueChange((it * 360f).coerceIn(0f, 360f)) },
        )
        PickerLabel(
            text = stringResource(R.string.appearance_picker_hue),
            color = onSurface.copy(alpha = 0.7f),
            miuix = miuix,
        )

        GradientBar(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.hsv(hue, saturation, value).copy(alpha = 0f),
                    Color.hsv(hue, saturation, value),
                ),
            ),
            fraction = alpha,
            thumbColor = thumbColor,
            outline = outline,
            onChange = { onAlphaChange(it.coerceIn(0f, 1f)) },
        )
        PickerLabel(
            text = stringResource(R.string.appearance_picker_alpha),
            color = onSurface.copy(alpha = 0.7f),
            miuix = miuix,
        )

        if (miuix) {
            MiuixText(
                text = stringResource(R.string.appearance_picker_notice),
                style = MiuixTheme.textStyles.body2,
                color = primary,
            )
        } else {
            MaterialText(
                text = stringResource(R.string.appearance_picker_notice),
                style = MaterialTheme.typography.bodySmall,
                color = primary,
            )
        }
    }
}

@Composable
private fun HexColorField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    isError: Boolean,
    miuix: Boolean,
) {
    if (miuix) {
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChange(it.isFocused) },
            label = stringResource(R.string.appearance_picker_hex),
            useLabelAsPlaceholder = true,
            singleLine = true,
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChange(it.isFocused) },
            label = { MaterialText(stringResource(R.string.appearance_picker_hex)) },
            singleLine = true,
            isError = isError,
        )
    }
}

@Composable
private fun PickerLabel(
    text: String,
    color: Color,
    miuix: Boolean,
) {
    if (miuix) {
        MiuixText(
            text = text,
            color = color,
            style = MiuixTheme.textStyles.body2,
        )
    } else {
        MaterialText(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SaturationValueField(
    hue: Float,
    saturation: Float,
    value: Float,
    outline: Color,
    onChange: (Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        }
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = androidx.compose.ui.geometry.Offset(
                saturation * size.width,
                (1f - value) * size.height,
            )
            drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = 9.dp.toPx(), center = center)
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(hue) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onChange(
                            (down.position.x / size.width).coerceIn(0f, 1f),
                            (1f - down.position.y / size.height).coerceIn(0f, 1f),
                        )
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    onChange(
                                        (change.position.x / size.width).coerceIn(0f, 1f),
                                        (1f - change.position.y / size.height).coerceIn(0f, 1f),
                                    )
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                },
        )
    }
}

@Composable
private fun GradientBar(
    brush: Brush,
    fraction: Float,
    thumbColor: Color,
    outline: Color,
    onChange: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(CircleShape)
            .background(brush)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onChange((down.position.x / size.width).coerceIn(0f, 1f))
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                onChange((change.position.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val x = fraction.coerceIn(0f, 1f) * size.width
            drawCircle(
                color = outline,
                radius = 10.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, size.height / 2f),
            )
            drawCircle(
                color = thumbColor,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, size.height / 2f),
            )
        }
    }
}

package aefox.reyang.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import aefox.reyang.ui.theme.themepackage.AppearanceColorToken
import aefox.reyang.ui.theme.themepackage.ResolvedColorTokens
import aefox.reyang.ui.theme.themepackage.ResolvedImageEffects
import top.yukonga.miuix.kmp.theme.Colors

private fun ResolvedColorTokens.color(token: AppearanceColorToken): Color? =
    values[token]?.let(::Color)

/**
 * Applies the semantic overlay to a generated Miuix color scheme: only
 * replaces tokens provided by the package, the rest keeps the generated
 * colors.
 *
 * The mapping lives in one place: Miuix role names are library implementation
 * details (may change across versions) while JSON token names are part of the
 * theme package format (immutable), so the token -> role mapping is only
 * allowed in this file.
 */
fun Colors.withAppearance(tokens: ResolvedColorTokens?): Colors {
    if (tokens == null || tokens.isEmpty) return this
    val accent = tokens.color(AppearanceColorToken.ACCENT)
    val onAccent = tokens.color(AppearanceColorToken.ON_ACCENT)
    val textPrimary = tokens.color(AppearanceColorToken.TEXT_PRIMARY)
    val textSecondary = tokens.color(AppearanceColorToken.TEXT_SECONDARY)
    val textDisabled = tokens.color(AppearanceColorToken.TEXT_DISABLED)
    val surfaceColor = tokens.color(AppearanceColorToken.SURFACE)
    val card = tokens.color(AppearanceColorToken.CARD)
    val outlineColor = tokens.color(AppearanceColorToken.OUTLINE)
    val errorColor = tokens.color(AppearanceColorToken.ERROR)

    return copy(
        primary = accent ?: primary,
        primaryVariant = accent ?: primaryVariant,
        onPrimary = onAccent ?: onPrimary,
        onPrimaryVariant = onAccent ?: onPrimaryVariant,
        error = errorColor ?: error,
        onBackground = textPrimary ?: onBackground,
        onBackgroundVariant = textSecondary ?: onBackgroundVariant,
        onSurface = textPrimary ?: onSurface,
        onSurfaceSecondary = textPrimary ?: onSurfaceSecondary,
        onSurfaceContainer = textPrimary ?: onSurfaceContainer,
        onSurfaceContainerVariant = textPrimary ?: onSurfaceContainerVariant,
        onSurfaceContainerHigh = textPrimary ?: onSurfaceContainerHigh,
        onSurfaceContainerHighest = textPrimary ?: onSurfaceContainerHighest,
        onSurfaceVariantSummary = textSecondary ?: onSurfaceVariantSummary,
        onSurfaceVariantActions = textSecondary ?: onSurfaceVariantActions,
        disabledOnSurface = textDisabled ?: disabledOnSurface,
        disabledOnSecondary = textDisabled ?: disabledOnSecondary,
        disabledOnSecondaryVariant = textDisabled ?: disabledOnSecondaryVariant,
        surface = surfaceColor ?: surface,
        background = surfaceColor ?: background,
        surfaceContainer = card ?: surfaceContainer,
        surfaceContainerHigh = card ?: surfaceContainerHigh,
        surfaceContainerHighest = card ?: surfaceContainerHighest,
        outline = outlineColor ?: outline,
        dividerLine = outlineColor ?: dividerLine,
    )
}

/**
 * Material 3 counterpart. Material has no disabled text role (components lower
 * alpha themselves), so textDisabled is deliberately not mapped to Material.
 */
fun ColorScheme.withAppearance(tokens: ResolvedColorTokens?): ColorScheme {
    if (tokens == null || tokens.isEmpty) return this
    val accent = tokens.color(AppearanceColorToken.ACCENT)
    val onAccent = tokens.color(AppearanceColorToken.ON_ACCENT)
    val textPrimary = tokens.color(AppearanceColorToken.TEXT_PRIMARY)
    val textSecondary = tokens.color(AppearanceColorToken.TEXT_SECONDARY)
    val surfaceColor = tokens.color(AppearanceColorToken.SURFACE)
    val card = tokens.color(AppearanceColorToken.CARD)
    val outlineColor = tokens.color(AppearanceColorToken.OUTLINE)
    val errorColor = tokens.color(AppearanceColorToken.ERROR)

    return copy(
        primary = accent ?: primary,
        onPrimary = onAccent ?: onPrimary,
        error = errorColor ?: error,
        onBackground = textPrimary ?: onBackground,
        onSurface = textPrimary ?: onSurface,
        onSurfaceVariant = textSecondary ?: onSurfaceVariant,
        surface = surfaceColor ?: surface,
        background = surfaceColor ?: background,
        surfaceContainer = card ?: surfaceContainer,
        surfaceContainerLow = card ?: surfaceContainerLow,
        surfaceContainerHigh = card ?: surfaceContainerHigh,
        surfaceContainerHighest = card ?: surfaceContainerHighest,
        outline = outlineColor ?: outline,
        outlineVariant = outlineColor ?: outlineVariant,
    )
}

/**
 * Color adjustment filter for the background image (saturation/contrast/
 * brightness); matrix construction mirrors SukiSU exactly. Returns null when
 * there is no overlay or colors are unadjusted (no filter applied); sharpen
 * has no equivalent here and is not applied.
 */
fun appearanceImageColorFilter(effects: ResolvedImageEffects?): ColorFilter? {
    if (effects == null || !effects.affectsColor) return null
    val saturationFactor = effects.saturation / 100f
    val contrastFactor = effects.contrast / 100f
    val brightnessOffset = effects.brightness / 100f * 255f
    val contrastOffset = 128f * (1f - contrastFactor) + brightnessOffset

    // Saturation matrix followed by the contrast/brightness offset matrix (same as SukiSU toColorMatrix).
    val androidMatrix = android.graphics.ColorMatrix()
    androidMatrix.setSaturation(saturationFactor)
    androidMatrix.postConcat(
        android.graphics.ColorMatrix(
            floatArrayOf(
                contrastFactor, 0f, 0f, 0f, contrastOffset,
                0f, contrastFactor, 0f, 0f, contrastOffset,
                0f, 0f, contrastFactor, 0f, contrastOffset,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
    )
    return ColorFilter.colorMatrix(ColorMatrix(androidMatrix.array))
}

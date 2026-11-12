package aefox.reyang.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import aefox.reyang.ui.component.liquid.rememberCombinedBackdrop
import aefox.reyang.ui.theme.LocalAppBackgroundBackdrop
import aefox.reyang.ui.theme.LocalEnableProgressiveBlur
import aefox.reyang.ui.theme.LocalThemeVisuals
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Blur radius shared by the standard bar blur and its progressive variant (mirrors SukiSU BarBlurRadius). */
private const val BarBlurRadius = 25f

/**
 * Without a background the bar tint must be strong enough to stay readable;
 * with a background it follows the card alpha (mirrors SukiSU
 * OpaqueBarTintAlpha / cardAlpha semantics).
 */
private const val OpaqueBarTintAlpha = 0.87f

@Composable
fun rememberBlurBackdrop(
    enableBlur: Boolean,
    allowProgressive: Boolean = true,
): LayerBackdrop? {
    // The progressive mode draws a blur band even when the plain bar blur is off, so it needs the
    // same page capture. Callers that only want the page layer for another purpose can opt out.
    val progressiveBlur = allowProgressive && LocalEnableProgressiveBlur.current && isRuntimeShaderSupported()
    if (!enableBlur && !progressiveBlur) return null
    if (!isRenderEffectSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    // With a custom background the page layer stays transparent so the background shows through; BlurredBar composites the background layer back at sampling time.
    val hasCustomBackground = rememberThemeBackgroundActive()
    return rememberLayerBackdrop {
        if (!hasCustomBackground) {
            drawRect(surfaceColor)
        }
        drawContent()
    }
}

/**
 * Composites the global background layer beneath the page sampling layer to
 * form the full backdrop.
 *
 * With a background the page layer is transparent in undrawn areas; blurring
 * it directly smears black into the bar. The background image fills exactly
 * those pixels, so stacking both layers restores the true scene behind the
 * bar. Falls back to the page layer itself when no background layer exists
 * (it is already backed by an opaque surface color); returns null when
 * neither is available and callers degrade to a translucent surface color.
 */
@Composable
fun rememberBackdropWithAppBackground(backdrop: Backdrop?): Backdrop? {
    if (backdrop == null) return null
    val appBackground = LocalAppBackgroundBackdrop.current
    if (appBackground != null) return rememberCombinedBackdrop(appBackground, backdrop)
    return backdrop.takeIf { !rememberThemeBackgroundActive() }
}

/**
 * Modifier that paints the progressive edge blur behind a bar (mirrors SukiSU
 * rememberProgressiveBarBlurModifier).
 *
 * The blur ramps from [gradient]'s strong edge (usually the screen edge) to a fully sharp,
 * full-resolution clear end, so content fades out of blur instead of meeting the bar with a hard
 * line. Works over a custom background (the wallpaper is layered under the page capture) and
 * without one (the page layer is opaque, so only scrolling content changes).
 *
 * [shape] clips the effect, so a floating pill only blurs inside its own outline and leaves the
 * page elements around it untouched.
 */
@Composable
fun rememberProgressiveBarBlurModifier(
    backdrop: Backdrop?,
    gradient: ProgressiveBlur,
    tint: Color,
    tintAlpha: Float,
    blurRadius: Float = BarBlurRadius,
    shape: Shape = RectangleShape,
): Modifier {
    if (!isRuntimeShaderSupported()) return Modifier
    val effectiveBackdrop = rememberBackdropWithAppBackground(backdrop) ?: return Modifier
    return remember(effectiveBackdrop, gradient, tint, tintAlpha, blurRadius, shape) {
        Modifier.progressiveTextureBlur(
            backdrop = effectiveBackdrop,
            shape = shape,
            blurRadius = blurRadius,
            gradient = gradient,
            colors = BlurColors(
                blendColors = listOf(BlendColorEntry(color = tint.copy(alpha = tintAlpha))),
            ),
        )
    }
}

@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurActive: Boolean = true,
    progressiveGradient: ProgressiveBlur = ProgressiveBlur.Top,
    content: @Composable () -> Unit,
) {
    val surfaceColor = MiuixTheme.colorScheme.surface
    val hasCustomBackground = rememberThemeBackgroundActive()
    val cardAlpha = LocalThemeVisuals.current.cardBackgroundAlphaPercent / 100f
    val progressiveBlur = LocalEnableProgressiveBlur.current && isRuntimeShaderSupported()
    val blurBackdrop = rememberBackdropWithAppBackground(backdrop)
    val tintAlpha = if (hasCustomBackground) cardAlpha else OpaqueBarTintAlpha

    val blurModifier = when {
        // Always active: over a custom background the wallpaper edge should be frosted even before
        // the page is scrolled, and without one the flat surface keeps the band invisible at rest.
        progressiveBlur -> rememberProgressiveBarBlurModifier(
            backdrop = backdrop,
            gradient = progressiveGradient,
            tint = surfaceColor,
            tintAlpha = tintAlpha,
        )
        blurActive && blurBackdrop != null -> Modifier.textureBlur(
            backdrop = blurBackdrop,
            shape = RectangleShape,
            blurRadius = BarBlurRadius,
            colors = BlurColors(
                blendColors = listOf(
                    BlendColorEntry(color = surfaceColor.copy(alpha = tintAlpha)),
                ),
            ),
        )
        // Frosted blur unavailable but a background exists: degrade to a surface color at the card alpha to avoid smearing transparent pixels into black.
        blurActive && hasCustomBackground -> Modifier.background(surfaceColor.copy(alpha = cardAlpha))
        else -> Modifier
    }

    Box(modifier = blurModifier) {
        // Readability veil over a bright wallpaper (mirrors SukiSU TopBarVeil):
        // white bar icons and titles disappear on a light image, so lay a soft
        // top-down scrim under the bar content. Skipped in the progressive-blur
        // path, whose tint band already covers the same pixels.
        if (hasCustomBackground && !progressiveBlur) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to surfaceColor.copy(alpha = 0.28f),
                            0.65f to surfaceColor.copy(alpha = 0.12f),
                            1f to Color.Transparent,
                        )
                    )
            )
        }
        content()
    }
}

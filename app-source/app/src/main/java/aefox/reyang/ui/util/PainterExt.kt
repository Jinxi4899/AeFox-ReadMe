package aefox.reyang.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.max

/**
 * Draws [painter] covering the current drawing area (center-cropped,
 * equivalent to ContentScale.Crop).
 *
 * `Painter.draw(size)` stretches the image regardless of aspect ratio, which
 * squashes card backgrounds whose ratio differs from the card; scale up
 * proportionally and center instead, letting the outer clip cut the overflow.
 * Falls back to a plain draw when intrinsicSize is unavailable. Callers must
 * clip to the target shape first or the image will bleed past the corners.
 */
fun Modifier.drawPainterCover(painter: Painter): Modifier = drawBehind {
    if (size.width <= 0f || size.height <= 0f) return@drawBehind
    val intrinsicSize = painter.intrinsicSize
    val usable = intrinsicSize.isSpecified && !intrinsicSize.isEmpty() &&
        intrinsicSize.width.isFinite() && intrinsicSize.height.isFinite()
    if (!usable) {
        with(painter) { draw(size) }
        return@drawBehind
    }
    val scale = max(size.width / intrinsicSize.width, size.height / intrinsicSize.height)
    val coveredSize = Size(intrinsicSize.width * scale, intrinsicSize.height * scale)
    translate(
        left = (size.width - coveredSize.width) / 2f,
        top = (size.height - coveredSize.height) / 2f,
    ) {
        with(painter) { draw(coveredSize) }
    }
}

package aefox.reyang.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import aefox.reyang.ui.theme.themepackage.HomeCardSticker
import aefox.reyang.ui.theme.themepackage.HomeCardStickerTransform
import java.io.File

/**
 * Draws all stickers over the status card in list order.
 *
 * [cardWidth] is the measured card width; position and size are stored as
 * ratios of it so stickers keep consistent relative placement on the home
 * card, preview and editing stage. When [heightNormalized] is true (immersive
 * banner), vertical position uses the card height and size is capped by the
 * card height; classic cards keep the old width-only computation.
 */
@Composable
fun HomeCardStickerOverlays(
    stickers: List<HomeCardSticker>,
    cardWidth: Dp,
    cardHeight: Dp = 0.dp,
    heightNormalized: Boolean = false,
    modifier: Modifier = Modifier,
) {
    stickers.forEach { sticker ->
        key(sticker.path) {
            HomeCardStickerOverlay(
                path = sticker.path,
                transform = sticker.transform,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                heightNormalized = heightNormalized,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun HomeCardStickerOverlay(
    path: String,
    transform: HomeCardStickerTransform,
    cardWidth: Dp,
    cardHeight: Dp,
    heightNormalized: Boolean,
    modifier: Modifier = Modifier,
) {
    if (path.isBlank()) return
    val size = if (heightNormalized && cardHeight > 0.dp) {
        cardWidth * transform.size.coerceAtMost(
            transform.maxSizeFor(cardWidth.value, cardHeight.value)
        )
    } else {
        cardWidth * transform.size
    }
    val rawX = (cardWidth - size) / 2 + cardWidth * transform.offsetX
    val x = if (heightNormalized && cardWidth > 0.dp) {
        rawX.coerceIn(0.dp, (cardWidth - size).coerceAtLeast(0.dp))
    } else {
        rawX
    }
    val y = if (heightNormalized && cardHeight > 0.dp) {
        val raw = (cardHeight - size) / 2 + cardHeight * transform.resolvedOffsetYPct()
        val maxY = (cardHeight - size).coerceAtLeast(0.dp)
        raw.coerceIn(0.dp, maxY)
    } else {
        (cardWidth - size) / 2 + cardWidth * transform.offsetY
    }
    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(path))
                .crossfade(true)
                .build()
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .offset(x = x, y = y)
            .size(size)
            .graphicsLayer { rotationZ = transform.rotation }
            .clip(RoundedCornerShape(size * 0.24f)),
    )
}

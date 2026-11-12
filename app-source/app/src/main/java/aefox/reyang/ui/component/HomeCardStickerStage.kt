package aefox.reyang.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aefox.reyang.BuildConfig
import aefox.reyang.R
import aefox.reyang.ui.screen.home.HomeStatusUi
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeCardSticker
import aefox.reyang.ui.theme.themepackage.HomeCardStickerTransform
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle
import aefox.reyang.ui.theme.UiText
import aefox.reyang.ui.util.drawPainterCover
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import java.io.File

/**
 * Editor stage: the same card proportions as the home status card with draggable
 * stickers (mirrors SukiSU's HomeCardStickerStage). The selected sticker follows
 * the drag and commits on gesture end; tapping another sticker selects it.
 */
@Composable
fun HomeCardStickerStage(
    backgroundImagePath: String,
    stickers: List<HomeCardSticker>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
    containerColor: Color,
    contentColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    layout: HomeLayout = HomeLayout.CLASSIC,
    imageStyle: HomeCardImageStyle = HomeCardImageStyle.BANNER,
    statusScale: Float = HomeStatusStyle.SCALE_DEFAULT,
    statusWidthFraction: Float = HomeStatusStyle.WIDTH_DEFAULT,
    pillScale: Float = HomeStatusStyle.PILL_SCALE_DEFAULT,
    statusAlignment: HomeStatusAlignment = HomeStatusAlignment.START,
) {
    if (layout == HomeLayout.IMMERSIVE) {
        ImmersiveBannerStickerStage(
            backgroundImagePath = backgroundImagePath,
            stickers = stickers,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            onTransformChange = onTransformChange,
            onTransformCommit = onTransformCommit,
            containerColor = containerColor,
            contentColor = contentColor,
            accentColor = accentColor,
            statusScale = statusScale,
            imageStyle = imageStyle,
            statusWidthFraction = statusWidthFraction,
            pillScale = pillScale,
            statusAlignment = statusAlignment,
            modifier = modifier,
        )
        return
    }
    ClassicStickerStage(
        backgroundImagePath = backgroundImagePath,
        stickers = stickers,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        onTransformChange = onTransformChange,
        onTransformCommit = onTransformCommit,
        containerColor = containerColor,
        contentColor = contentColor,
        accentColor = accentColor,
        modifier = modifier,
    )
}

/** Thumbnail of one sticker (add/remove list under the stage). */
@Composable
fun HomeCardStickerThumbnail(
    path: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(path))
                .crossfade(true)
                .build()
        ),
        contentDescription = stringResource(R.string.settings_home_card_sticker),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .then(if (selected) Modifier.border(2.dp, accentColor, shape) else Modifier)
            .clickable(onClick = onClick),
    )
}

/** Classic stage: the 360:196 status card mock with width-normalized sticker offsets. */
@Composable
private fun ClassicStickerStage(
    backgroundImagePath: String,
    stickers: List<HomeCardSticker>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
    containerColor: Color,
    contentColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backgroundPainter = backgroundImagePath.takeIf { it.isNotBlank() && File(it).isFile }?.let {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context).data(File(it)).crossfade(true).build()
        )
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(360f / 196f)
            .clip(RoundedCornerShape(26.dp))
            .then(
                if (backgroundPainter != null) {
                    Modifier.drawPainterCover(backgroundPainter)
                } else {
                    Modifier.background(containerColor)
                }
            )
    ) {
        val cardWidth = maxWidth
        val widthPx = with(LocalDensity.current) { cardWidth.toPx() }
        val heightRatio = with(LocalDensity.current) {
            if (widthPx > 0f) maxHeight.toPx() / widthPx else 1f
        }
        // SukiSU stage parity: the classic card mock opens with a tag row above the title.
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusTag(
                text = stringResource(R.string.home_tag_framework),
                backgroundColor = accentColor,
                textColor = Color.White,
            )
            StatusTag(
                text = stringResource(R.string.module_status_active),
                backgroundColor = contentColor.copy(alpha = 0.18f),
                textColor = contentColor,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
        ) {
            Text(
                text = stringResource(R.string.module_status_active),
                fontSize = UiText.Hero,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Text(
                text = BuildConfig.VERSION_DISPLAY,
                fontSize = UiText.Tag,
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.75f),
            )
        }
        stickers.forEachIndexed { index, sticker ->
            key(sticker.path) {
                ClassicStickerItem(
                    sticker = sticker,
                    index = index,
                    stickers = stickers,
                    selectedIndex = selectedIndex,
                    cardWidth = cardWidth,
                    widthPx = widthPx,
                    heightRatio = heightRatio,
                    onSelect = onSelect,
                    onTransformChange = onTransformChange,
                    onTransformCommit = onTransformCommit,
                )
            }
        }
    }
}

@Composable
private fun ClassicStickerItem(
    sticker: HomeCardSticker,
    index: Int,
    stickers: List<HomeCardSticker>,
    selectedIndex: Int,
    cardWidth: androidx.compose.ui.unit.Dp,
    widthPx: Float,
    heightRatio: Float,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
) {
    val transform = sticker.transform
    val size = cardWidth * transform.size
    val base = (cardWidth - size) / 2
    val gesture = stickerGesture(
        index = index,
        selected = index == selectedIndex,
        widthPx = widthPx,
        heightPx = 0f,
        stickers = stickers,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        onTransformChange = onTransformChange,
        onTransformCommit = onTransformCommit,
        maxOffsetY = (heightRatio - 0.5f).coerceAtLeast(-0.5f),
    )
    Box(
        modifier = Modifier
            .offset(x = base + cardWidth * transform.offsetX, y = base + cardWidth * transform.offsetY)
            .size(size)
            .then(gesture),
    ) {
        StickerImage(path = sticker.path, rotation = transform.rotation, size = size)
    }
}

/** Immersive stage: a non-interactive pill preview plus the image banner holding the stickers. */
@Composable
private fun ImmersiveBannerStickerStage(
    backgroundImagePath: String,
    stickers: List<HomeCardSticker>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
    containerColor: Color,
    contentColor: Color,
    accentColor: Color,
    statusScale: Float,
    imageStyle: HomeCardImageStyle,
    statusWidthFraction: Float,
    pillScale: Float,
    statusAlignment: HomeStatusAlignment,
    modifier: Modifier = Modifier,
) {
    if (imageStyle != HomeCardImageStyle.BANNER) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(360f / 120f)
                .clip(RoundedCornerShape(20.dp))
                .background(containerColor)
        )
        return
    }
    val scale = HomeStatusStyle.coerceScale(statusScale)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (statusAlignment == HomeStatusAlignment.END) {
            Alignment.End
        } else {
            Alignment.Start
        },
        verticalArrangement = Arrangement.spacedBy(HomeStatusUi.ClusterGap),
    ) {
        StagePillPreview(
            containerColor = containerColor,
            contentColor = contentColor,
            accentColor = accentColor,
            pillScale = pillScale,
        )
        BannerStickerCanvas(
            backgroundImagePath = backgroundImagePath,
            stickers = stickers,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            onTransformChange = onTransformChange,
            onTransformCommit = onTransformCommit,
            containerColor = containerColor,
            widthFraction = HomeStatusStyle.coerceWidth(statusWidthFraction),
            scale = scale,
        )
    }
}

@Composable
private fun BannerStickerCanvas(
    backgroundImagePath: String,
    stickers: List<HomeCardSticker>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
    containerColor: Color,
    widthFraction: Float,
    scale: Float,
) {
    val context = LocalContext.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(HomeStatusUi.scaledDp(HomeStatusUi.BannerHeight, scale))
            .clip(RoundedCornerShape(HomeStatusUi.BannerCorner))
            .background(containerColor)
    ) {
        if (backgroundImagePath.isNotBlank() && File(backgroundImagePath).isFile) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(backgroundImagePath))
                    .crossfade(true)
                    .precision(Precision.EXACT)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        stickers.forEachIndexed { index, sticker ->
            key(sticker.path) {
                BannerStickerItem(
                    sticker = sticker,
                    index = index,
                    selectedIndex = selectedIndex,
                    stickers = stickers,
                    cardWidth = maxWidth,
                    cardHeight = maxHeight,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    onSelect = onSelect,
                    onTransformChange = onTransformChange,
                    onTransformCommit = onTransformCommit,
                )
            }
        }
    }
}

@Composable
private fun BannerStickerItem(
    sticker: HomeCardSticker,
    index: Int,
    selectedIndex: Int,
    stickers: List<HomeCardSticker>,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    widthPx: Float,
    heightPx: Float,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
) {
    val transform = sticker.transform
    val sizeFraction = transform.size.coerceAtMost(
        transform.maxSizeFor(cardWidth.value, cardHeight.value)
    )
    val size = cardWidth * sizeFraction
    val x = ((cardWidth - size) / 2 + cardWidth * transform.offsetX)
        .coerceIn(0.dp, (cardWidth - size).coerceAtLeast(0.dp))
    val y = ((cardHeight - size) / 2 + cardHeight * transform.resolvedOffsetYPct())
        .coerceIn(0.dp, (cardHeight - size).coerceAtLeast(0.dp))
    val gesture = stickerGesture(
        index = index,
        selected = index == selectedIndex,
        widthPx = widthPx,
        heightPx = heightPx,
        stickers = stickers,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        onTransformChange = onTransformChange,
        onTransformCommit = onTransformCommit,
        maxOffsetY = -1f,
    )
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .then(gesture),
    ) {
        StickerImage(path = sticker.path, rotation = transform.rotation, size = size)
    }
}

@Composable
private fun StickerImage(path: String, rotation: Float, size: androidx.compose.ui.unit.Dp) {
    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(path))
                .crossfade(true)
                .build()
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationZ = rotation }
            .clip(RoundedCornerShape(size * 0.24f)),
    )
}

/**
 * Shared sticker gestures: drag moves the selected sticker (commit on release),
 * tap selects an unselected one. [maxOffsetY] < 0 switches to the height-normalized
 * banner math (offsets bounded by the sticker's half size).
 */
@Composable
private fun stickerGesture(
    index: Int,
    selected: Boolean,
    widthPx: Float,
    heightPx: Float,
    stickers: List<HomeCardSticker>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onTransformChange: (Int, HomeCardStickerTransform) -> Unit,
    onTransformCommit: (Int, HomeCardStickerTransform) -> Unit,
    maxOffsetY: Float,
): Modifier {
    val currentStickers by rememberUpdatedState(stickers)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnTransformChange by rememberUpdatedState(onTransformChange)
    val currentOnTransformCommit by rememberUpdatedState(onTransformCommit)
    val currentWidthPx by rememberUpdatedState(widthPx)
    val currentHeightPx by rememberUpdatedState(heightPx)
    if (!selected) {
        return Modifier.pointerInput(index, selected) {
            detectTapGestures {
                if (currentSelectedIndex != index) currentOnSelect(index)
            }
        }
    }
    return Modifier.pointerInput(index, selected) {
        var dragging: HomeCardStickerTransform? = null
        detectDragGestures(
            onDragStart = { dragging = currentStickers.getOrNull(index)?.transform },
            onDragEnd = {
                dragging?.let { currentOnTransformCommit(index, it) }
                dragging = null
            },
            onDragCancel = { dragging = null },
        ) { change, dragAmount ->
            change.consume()
            val base = dragging ?: currentStickers.getOrNull(index)?.transform
                ?: return@detectDragGestures
            dragging = if (maxOffsetY < 0f) {
                dragHeightNormalized(base, dragAmount, currentWidthPx, currentHeightPx)
            } else {
                dragWidthNormalized(base, dragAmount, currentWidthPx, maxOffsetY)
            }
            dragging?.let { currentOnTransformChange(index, it) }
        }
    }
}

/** Width-normalized drag (classic card): X/Y offsets are fractions of the stage width. */
private fun dragWidthNormalized(
    base: HomeCardStickerTransform,
    dragAmount: Offset,
    widthPx: Float,
    maxOffsetY: Float,
): HomeCardStickerTransform? {
    if (widthPx <= 0f) return null
    return base.copy(
        offsetX = (base.offsetX + dragAmount.x / widthPx).coerceIn(-0.5f, 0.5f),
        offsetY = (base.offsetY + dragAmount.y / widthPx).coerceIn(-0.5f, maxOffsetY),
    ).coerced()
}

/** Height-normalized drag (immersive banner): Y is a fraction of the banner height. */
private fun dragHeightNormalized(
    base: HomeCardStickerTransform,
    dragAmount: Offset,
    widthPx: Float,
    heightPx: Float,
): HomeCardStickerTransform? {
    if (widthPx <= 0f || heightPx <= 0f) return null
    val maxOffsetX = 0.5f
    return base.copy(
        offsetX = (base.offsetX + dragAmount.x / widthPx).coerceIn(-maxOffsetX, maxOffsetX),
    ).withHeightOffset(
        (base.resolvedOffsetYPct() + dragAmount.y / heightPx).coerceIn(-0.5f, 0.5f)
    ).coerced()
}

@Composable
private fun StagePillPreview(
    containerColor: Color,
    contentColor: Color,
    accentColor: Color,
    pillScale: Float,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.12f), shape)
            .padding(
                horizontal = HomeStatusUi.pillDp(HomeStatusUi.PillHorizontalPadding, pillScale),
                vertical = HomeStatusUi.pillDp(8.dp, pillScale),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            HomeStatusUi.pillDp(HomeStatusUi.PillContentGap, pillScale)
        ),
    ) {
        Box(
            modifier = Modifier
                .size(HomeStatusUi.pillDp(HomeStatusUi.DotSize, pillScale))
                .clip(CircleShape)
                .background(accentColor)
        )
        Text(
            text = stringResource(R.string.module_status_active),
            color = contentColor,
            fontSize = HomeStatusUi.pillSp(HomeStatusUi.LabelSizeSp, pillScale),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .width(HomeStatusUi.pillDp(HomeStatusUi.DividerWidth, pillScale))
                .height(HomeStatusUi.pillDp(HomeStatusUi.DividerHeight, pillScale))
                .background(contentColor.copy(alpha = 0.18f))
        )
        Text(
            text = "LKM · ${BuildConfig.VERSION_DISPLAY}",
            color = contentColor.copy(alpha = 0.58f),
            fontSize = HomeStatusUi.pillSp(HomeStatusUi.MetaSizeSp, pillScale),
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

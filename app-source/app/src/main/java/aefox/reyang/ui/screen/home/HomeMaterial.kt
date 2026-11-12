package aefox.reyang.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import aefox.reyang.R
import aefox.reyang.core.system.PermissionState
import aefox.reyang.ui.component.AeFoxIcon
import aefox.reyang.ui.component.HomeCardStickerOverlays
import aefox.reyang.ui.component.StatusTag
import aefox.reyang.ui.component.WarningLevel
import aefox.reyang.ui.component.material.TonalCard
import aefox.reyang.ui.component.material.SegmentedColumn
import aefox.reyang.ui.component.material.SegmentedListItem
import aefox.reyang.ui.theme.LocalThemeVisuals
import aefox.reyang.ui.theme.backgroundAwareColor
import aefox.reyang.ui.theme.isInDarkTheme
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import aefox.reyang.ui.theme.rememberThemeHomeCardStickers
import aefox.reyang.ui.theme.shouldShowHomeCardImage
import aefox.reyang.ui.theme.themeHomeCardImageFile
import aefox.reyang.ui.theme.themeCustomText
import aefox.reyang.ui.theme.themepackage.HomeCardImageStyle
import aefox.reyang.ui.theme.themepackage.HomeLayout
import aefox.reyang.ui.theme.themepackage.HomeStatusAlignment
import aefox.reyang.ui.theme.themepackage.HomeStatusStyle
import aefox.reyang.ui.util.drawPainterCover

/** Classic status card shape (SukiSU HeroShape). */
private val HeroShape = RoundedCornerShape(28.dp)

@Composable
fun HomePagerMaterial(
    state: HomeUiState,
    permissionState: PermissionState,
    activationActive: Boolean,
    actions: HomeActions,
    bottomInnerPadding: Dp,
    isRefreshing: Boolean = false,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    // With a theme package background the page scaffold must stay transparent so the
    // global background layer shows through (mirrors SukiSU ExpressiveScaffold).
    val hasCustomBackground = rememberThemeBackgroundActive()

    Scaffold(
        containerColor = if (hasCustomBackground) Color.Transparent else MaterialTheme.colorScheme.surface,
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = actions.onRefresh,
            state = pullToRefreshState,
        ) {
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning banner appears until every required app permission is granted.
            AnimatedVisibility(
                visible = !permissionState.requiredGranted,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                WarningCard(
                    message = stringResource(R.string.home_permission_warning),
                )
            }
            // Activation status card: immersive layout renders a compact pill + optional image banner.
            val homeLayout = LocalThemeVisuals.current.homeLayout
            if (homeLayout == HomeLayout.IMMERSIVE) {
                ImmersiveStatusCardMaterial(
                    activationActive = activationActive,
                    appVersion = state.systemInfo.appVersion,
                )
            } else {
                ModuleStatusCard(activationActive, state.systemInfo.appVersion)
            }
            InfoCard(systemInfo = state.systemInfo)
            Spacer(Modifier.height(bottomInnerPadding))
        }
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Home title: customHomeTitle first, then the custom text home.title, then the app name.
    val visuals = LocalThemeVisuals.current
    val title = visuals.customHomeTitle.takeIf { it.isNotBlank() }
        ?: themeCustomText("home.title", stringResource(R.string.app_name))
    // Keep the app bar see-through over a theme package background (mirrors SukiSU).
    val hasCustomBackground = rememberThemeBackgroundActive()
    val barColor = if (hasCustomBackground) Color.Transparent else MaterialTheme.colorScheme.surface
    LargeFlexibleTopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = barColor,
            scrolledContainerColor = barColor
        ),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ModuleStatusCard(
    active: Boolean,
    appVersion: String,
) {
    val visuals = LocalThemeVisuals.current
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val hasCardImage = cardImageFile != null
    val cardImagePainter = cardImageFile?.let { file ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(file)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
        )
    }
    // Hide text and decoration per package settings when a card background image exists.
    val hideStatusText = hasCardImage && visuals.hideHomeCardStatusText
    val hideDecoration = hasCardImage && visuals.hideHomeCardDecoration
    val stickers = rememberThemeHomeCardStickers()

    val containerColor =
        if (active) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.errorContainer
    val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
    val title = stringResource(
        if (active) R.string.module_status_active else R.string.module_status_inactive
    )
    val statusText = if (active) {
        themeCustomText("home.status_working", title)
    } else {
        title
    }

    // SukiSU parity: the classic card is content-driven with a 188dp floor, and the
    // cover image is cropped to the card box instead of stretching the card height.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeroShape)
            .then(if (cardImagePainter != null) Modifier.drawPainterCover(cardImagePainter) else Modifier),
        color = if (cardImagePainter != null) {
            Color.Transparent
        } else {
            backgroundAwareColor(containerColor)
        },
        contentColor = contentColor,
        shape = HeroShape,
    ) {
        BoxWithConstraints(
            modifier = if (hideDecoration) {
                Modifier.fillMaxWidth()
            } else {
                // Decoration sits behind the information block (SukiSU classic card rings).
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawStatusRings(contentColor.copy(alpha = 0.18f)) }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HomeStatusUi.ClassicMinHeight)
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // SukiSU classic card opens with a tag row: the host framework plus the
                // current activation state, mirroring the original mode/LKM badges.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusTag(
                        text = stringResource(R.string.home_tag_framework),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    StatusTag(
                        text = statusText,
                        backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        textColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (!hideStatusText) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = appVersion,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
            HomeCardStickerOverlays(stickers = stickers, cardWidth = maxWidth)
        }
    }
}

/** Classic-card decoration rings, drawn behind the information block (SukiSU Material layout). */
private fun DrawScope.drawStatusRings(color: Color) {
    val center = Offset(size.width - 36.dp.toPx(), 32.dp.toPx())
    val stroke = 2.dp.toPx()
    drawCircle(color = color, radius = 112.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(
        color = color,
        radius = 84.dp.toPx(),
        center = center,
        style = Stroke(
            stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 8.dp.toPx())),
        ),
    )
    drawCircle(color = color, radius = 56.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(color = color, radius = 18.dp.toPx(), center = center)
}

/**
 * Immersive status card: compact working pill + optional image banner (banner style),
 * positioned and scaled by homeStatusAlignment/Scale/WidthFraction and homePillScale.
 */
@Composable
private fun ImmersiveStatusCardMaterial(
    activationActive: Boolean,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    val visuals = LocalThemeVisuals.current
    val scale = HomeStatusStyle.coerceScale(visuals.homeStatusScale)
    val pillScale = HomeStatusStyle.coercePillScale(visuals.homePillScale)
    val widthFraction = HomeStatusStyle.coerceWidth(visuals.homeStatusWidthFraction)
    val context = LocalContext.current
    val dark = isInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val hasCustomBackground = rememberThemeBackgroundActive()
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val showBanner = visuals.homeCardImageStyle == HomeCardImageStyle.BANNER &&
        shouldShowHomeCardImage(
            cardBackgroundFile = cardImageFile,
            hasCustomBackground = hasCustomBackground,
            showWithoutBackground = visuals.showHomeCardImageWithoutBackground,
        )
    val borderColor = if (isInDarkTheme()) {
        Color.White.copy(alpha = 0.3f)
    } else {
        colors.outline.copy(alpha = 0.28f)
    }
    val dotColor = if (activationActive) colors.primary else colors.error
    val title = stringResource(
        if (activationActive) R.string.module_status_active else R.string.module_status_inactive
    )
    val label = if (activationActive) {
        themeCustomText("home.status_working", title)
    } else {
        title
    }
    val bannerShape = RoundedCornerShape(HomeStatusUi.BannerCorner)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (visuals.homeStatusAlignment == HomeStatusAlignment.END) {
            Alignment.End
        } else {
            Alignment.Start
        },
        verticalArrangement = Arrangement.spacedBy(HomeStatusUi.ClusterGap),
    ) {
        // Working status pill.
        ImmersiveStatusPillMaterial(
            dotColor = dotColor,
            label = label,
            meta = appVersion,
            pillScale = pillScale,
            hasCustomBackground = hasCustomBackground,
        )

        // Image banner: card background image + stickers (vertical offsets normalized to banner height).
        if (showBanner && cardImageFile != null) {
            // SukiSU parity: the banner keeps a fixed scaled height; the image is cover-cropped.
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(widthFraction)) {
                val bannerWidth = maxWidth
                val bannerHeight = HomeStatusUi.scaledDp(HomeStatusUi.BannerHeight, scale)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .clip(bannerShape)
                        .background(colors.surfaceContainer)
                        .border(1.dp, borderColor, bannerShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(cardImageFile)
                            .crossfade(true)
                            .precision(Precision.EXACT)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High,
                        modifier = Modifier.fillMaxSize(),
                    )
                    HomeCardStickerOverlays(
                        stickers = rememberThemeHomeCardStickers(),
                        cardWidth = bannerWidth,
                        cardHeight = bannerHeight,
                        heightNormalized = true,
                    )
                }
            }
        }
    }
}

/** Working status pill of the immersive status card: dot + label + divider + version meta. */
@Composable
private fun ImmersiveStatusPillMaterial(
    dotColor: Color,
    label: String,
    meta: String,
    pillScale: Float,
    hasCustomBackground: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.heightIn(min = HomeStatusUi.PillTouchTarget),
        shape = CircleShape,
        color = colors.surfaceContainer.copy(alpha = if (hasCustomBackground) 0.9f else 1f),
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier
                .heightIn(
                    min = maxOf(
                        HomeStatusUi.PillTouchTarget,
                        HomeStatusUi.pillDp(HomeStatusUi.PillMinHeight, pillScale),
                    )
                )
                .padding(
                    horizontal = HomeStatusUi.pillDp(HomeStatusUi.PillHorizontalPadding, pillScale)
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
                    .background(dotColor)
            )
            Text(
                text = label,
                color = colors.onSurface,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.LabelSizeSp, pillScale),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .width(HomeStatusUi.pillDp(HomeStatusUi.DividerWidth, pillScale))
                    .height(HomeStatusUi.pillDp(HomeStatusUi.DividerHeight, pillScale))
                    .background(colors.onSurface.copy(alpha = 0.18f))
            )
            Text(
                text = meta,
                modifier = Modifier.weight(1f, fill = false),
                color = colors.onSurfaceVariant,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.MetaSizeSp, pillScale),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WarningCard(
    message: String,
    color: Color = MaterialTheme.colorScheme.error,
    level: WarningLevel = WarningLevel.Error,
    onClick: (() -> Unit)? = null
) {
    val containerColor = if (level == WarningLevel.Notice) MaterialTheme.colorScheme.tertiaryContainer else color
    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (level == WarningLevel.Notice) 16.dp else 24.dp,
                    vertical = if (level == WarningLevel.Notice) 12.dp else 24.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AeFoxIcon(size = 26.dp, contentDescription = null)
            Spacer(Modifier.size(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.contentColorFor(containerColor),
            )
        }
    }
    if (onClick != null) {
        TonalCard(containerColor = containerColor, onClick = onClick, content = content)
    } else {
        TonalCard(containerColor = containerColor, content = content)
    }
}

@Composable
private fun InfoCard(systemInfo: SystemInfo) {
    @Composable
    fun InfoCardItem(icon: ImageVector, label: String, content: String) {
        SegmentedListItem(
            headlineContent = { Text(label, style = MaterialTheme.typography.bodyLarge) },
            leadingContent = { Icon(icon, contentDescription = null) },
            supportingContent = {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
    SegmentedColumn(content = listOf(
        {
            InfoCardItem(
                Icons.Filled.Smartphone,
                themeCustomText(
                    "home.info_device_model",
                    stringResource(R.string.home_device_model),
                ),
                systemInfo.deviceModel,
            )
        },
        { InfoCardItem(Icons.Filled.Android, stringResource(R.string.home_android_version), systemInfo.androidVersion) },
    ))
}

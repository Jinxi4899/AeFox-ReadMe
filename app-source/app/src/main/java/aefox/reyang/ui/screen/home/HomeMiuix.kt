package aefox.reyang.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
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
import aefox.reyang.ui.component.HomeCardStickerOverlays
import aefox.reyang.ui.component.StatusTag
import aefox.reyang.ui.component.WarningLevel
import aefox.reyang.ui.component.miuix.WarningCard
import aefox.reyang.ui.theme.LocalEnableBlur
import aefox.reyang.ui.theme.LocalThemeVisuals
import aefox.reyang.ui.theme.UiText
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
import aefox.reyang.ui.util.BlurredBar
import aefox.reyang.ui.util.drawPainterCover
import aefox.reyang.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun HomePagerMiuix(
    state: HomeUiState,
    permissionState: PermissionState,
    activationActive: Boolean,
    actions: HomeActions,
    bottomInnerPadding: Dp,
    isRefreshing: Boolean = false,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    // With a theme package background the page scaffold must stay transparent so the
    // global background layer shows through (mirrors SukiSU AppScaffold).
    val hasCustomBackground = rememberThemeBackgroundActive()
    val barColor = if (blurActive || hasCustomBackground) Color.Transparent else colorScheme.surface
    val pullToRefreshState = rememberPullToRefreshState()
    val refreshTexts = listOf(
        stringResource(R.string.refresh_pulling),
        stringResource(R.string.refresh_release),
        stringResource(R.string.refresh_refresh),
        stringResource(R.string.refresh_complete),
    )
    Scaffold(
        containerColor = if (hasCustomBackground) Color.Transparent else colorScheme.surface,
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                backdrop = backdrop,
                barColor = barColor,
            )
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        PullToRefresh(
            isRefreshing = isRefreshing,
            pullToRefreshState = pullToRefreshState,
            onRefresh = actions.onRefresh,
            refreshTexts = refreshTexts,
            contentPadding = innerPadding,
        ) {
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Warning banner appears until every required app permission is granted.
                        AnimatedVisibility(
                            visible = !permissionState.requiredGranted,
                            enter = fadeIn() + expandVertically(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            WarningCard(
                                message = stringResource(R.string.home_permission_warning),
                                level = WarningLevel.Error,
                            )
                        }
                        // Activation status card: immersive layout renders a compact pill + optional image banner.
                        val homeLayout = LocalThemeVisuals.current.homeLayout
                        if (homeLayout == HomeLayout.IMMERSIVE) {
                            ImmersiveStatusCardMiuix(
                                activationActive = activationActive,
                                appVersion = state.systemInfo.appVersion,
                            )
                        } else {
                            ModuleStatusCardMiuix(activationActive, state.systemInfo.appVersion)
                        }
                        InfoCard(systemInfo = state.systemInfo)
                    }
                    Spacer(Modifier.height(bottomInnerPadding))
                }
            }
        }
        }
    }
}

@Composable
private fun ModuleStatusCardMiuix(
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

    val iconColor = if (active) Color(0xFF36D167) else Color(0xFFF72727)
    val containerColor = if (active) Color(0xFFDFFAE4) else Color(0xFFF8E2E2)
    val statusText = if (active) {
        themeCustomText("home.status_working", stringResource(R.string.module_status_active))
    } else {
        stringResource(R.string.module_status_inactive)
    }
    // SukiSU parity: the classic card is content-driven with a 188dp floor, and the
    // cover image is cropped to the card box instead of stretching the card height.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardDefaults.CornerRadius))
            .then(if (cardImagePainter != null) Modifier.drawPainterCover(cardImagePainter) else Modifier),
        colors = CardDefaults.defaultColors(
            color = if (cardImagePainter != null) {
                Color.Transparent
            } else {
                backgroundAwareColor(containerColor)
            },
        ),
    ) {
        BoxWithConstraints {
            // Reference classic card: a single check/cross badge anchored to the bottom
            // end and pushed slightly past the card edge (clipped by the card shape).
            if (!hideDecoration) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 27.dp, y = 31.dp)
                        .size(150.dp),
                    imageVector =
                        if (active) Icons.Rounded.CheckCircleOutline else Icons.Rounded.Cancel,
                    tint = iconColor,
                    contentDescription = null,
                )
            }
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusTag(
                        text = stringResource(R.string.home_tag_framework),
                        backgroundColor = iconColor,
                        textColor = Color.White,
                    )
                    StatusTag(
                        text = statusText,
                        backgroundColor = colorScheme.onSurface.copy(alpha = 0.10f),
                        textColor = colorScheme.onSurface,
                    )
                }
                if (!hideStatusText) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = statusText,
                            fontSize = UiText.Display,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = appVersion,
                            fontSize = UiText.Body,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            HomeCardStickerOverlays(stickers = stickers, cardWidth = maxWidth)
        }
    }
}

/**
 * Immersive status card: compact working pill + optional image banner (banner style),
 * positioned and scaled by homeStatusAlignment/Scale/WidthFraction and homePillScale.
 */
@Composable
private fun ImmersiveStatusCardMiuix(
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
    val hasCustomBackground = rememberThemeBackgroundActive()
    val cardImageFile = themeHomeCardImageFile(context, visuals.adaptive, dark)
    val borderColor = if (isInDarkTheme()) {
        Color.White.copy(alpha = 0.28f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.12f)
    }
    val dotColor = if (activationActive) Color(0xFF36D167) else Color(0xFFF72727)
    val label = if (activationActive) {
        themeCustomText("home.status_working", stringResource(R.string.module_status_active))
    } else {
        stringResource(R.string.module_status_inactive)
    }
    val bannerShape = RoundedCornerShape(HomeStatusUi.BannerCorner)
    val showBanner = visuals.homeCardImageStyle == HomeCardImageStyle.BANNER &&
        shouldShowHomeCardImage(
            cardBackgroundFile = cardImageFile,
            hasCustomBackground = hasCustomBackground,
            showWithoutBackground = visuals.showHomeCardImageWithoutBackground,
        )

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
        ImmersiveStatusPillMiuix(
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
                        .background(colorScheme.surfaceContainer)
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
private fun ImmersiveStatusPillMiuix(
    dotColor: Color,
    label: String,
    meta: String,
    pillScale: Float,
    hasCustomBackground: Boolean,
) {
    Card(
        modifier = Modifier
            .heightIn(min = HomeStatusUi.PillTouchTarget)
            .border(
                1.dp,
                colorScheme.onSurface.copy(alpha = if (hasCustomBackground) 0.14f else 0.08f),
                RoundedCornerShape(50),
            ),
        cornerRadius = HomeStatusUi.PillTouchTarget,
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(
            color = colorScheme.surfaceContainer.copy(alpha = if (hasCustomBackground) 0.88f else 1f),
            contentColor = colorScheme.onSurface,
        ),
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
                color = colorScheme.onSurface,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.LabelSizeSp, pillScale),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .width(HomeStatusUi.pillDp(HomeStatusUi.DividerWidth, pillScale))
                    .height(HomeStatusUi.pillDp(HomeStatusUi.DividerHeight, pillScale))
                    .background(colorScheme.onSurface.copy(alpha = 0.18f))
            )
            Text(
                text = meta,
                color = colorScheme.onSurfaceVariantSummary,
                fontSize = HomeStatusUi.pillSp(HomeStatusUi.MetaSizeSp, pillScale),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop?,
    barColor: Color,
) {
    // Home title: customHomeTitle first, then the custom text home.title, then the app name.
    val visuals = LocalThemeVisuals.current
    val title = visuals.customHomeTitle.takeIf { it.isNotBlank() }
        ?: themeCustomText("home.title", stringResource(R.string.app_name))
    BlurredBar(backdrop) {
        TopAppBar(
            color = barColor,
            title = title,
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun InfoCard(systemInfo: SystemInfo) {
    @Composable
    fun InfoText(
        icon: ImageVector,
        title: String,
        content: String,
        bottomPadding: Dp = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp).size(24.dp),
                tint = colorScheme.onSurface,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = MiuixTheme.textStyles.headline1.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = content,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            InfoText(
                icon = Icons.Filled.Smartphone,
                title = themeCustomText(
                    "home.info_device_model",
                    stringResource(R.string.home_device_model),
                ),
                content = systemInfo.deviceModel,
            )
            InfoText(
                icon = Icons.Filled.Android,
                title = stringResource(R.string.home_android_version),
                content = systemInfo.androidVersion,
                bottomPadding = 0.dp
            )
        }
    }
}

package aefox.reyang.ui.component.material

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import aefox.reyang.ui.theme.rememberThemeBackgroundActive

/**
 * Wrapper around the material3 [Scaffold] that follows the theme package
 * background (mirrors SukiSU AppScaffold): when [containerColor] is left
 * unspecified, the container becomes transparent while a custom background is
 * active and falls back to `surfaceContainer` otherwise. Passing an explicit
 * [containerColor] always takes precedence.
 */
@Composable
fun ExpressiveScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val resolvedContainerColor = when {
        containerColor != Color.Unspecified -> containerColor
        rememberThemeBackgroundActive() -> Color.Transparent
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = resolvedContainerColor,
        contentColor = if (contentColor != Color.Unspecified) {
            contentColor
        } else {
            contentColorFor(resolvedContainerColor)
        },
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

/**
 * Top bar colors shared by the expressive screens: with a theme package
 * background both the resting and scrolled container become transparent so the
 * wallpaper shows through the bar area; without one the given colors pass
 * through unchanged.
 */
@Composable
fun expressiveTopAppBarColors(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    scrolledContainerColor: Color = containerColor,
): TopAppBarColors {
    if (!rememberThemeBackgroundActive()) {
        return TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = scrolledContainerColor,
        )
    }
    return TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
    )
}

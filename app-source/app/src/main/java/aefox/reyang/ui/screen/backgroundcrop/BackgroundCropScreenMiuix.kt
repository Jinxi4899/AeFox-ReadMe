package aefox.reyang.ui.screen.backgroundcrop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aefox.reyang.R
import aefox.reyang.ui.theme.LocalEnableBlur
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.rememberThemeBackgroundActive
import aefox.reyang.ui.util.BlurredBar
import aefox.reyang.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/** Background crop (Miuix mode): preview area plus ratio/rotate/apply actions. */
@Composable
fun BackgroundCropScreenMiuix(
    state: BackgroundCropUiState,
    actions: BackgroundCropActions,
    preview: @Composable (Modifier) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(LocalEnableBlur.current)
    val barColor = if (backdrop != null || rememberThemeBackgroundActive()) {
        Color.Transparent
    } else {
        colorScheme.surface
    }

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.background_crop_title),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
                        }
                    },
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                preview(Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.background_crop_hint),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BackgroundCropRatio.entries.forEach { ratio ->
                    TextButton(
                        text = if (ratio == state.ratio) "✓ " + ratio.label() else ratio.label(),
                        onClick = { actions.onSelectRatio(ratio) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    text = stringResource(R.string.background_crop_rotate),
                    onClick = actions.onRotate,
                )
                TextButton(
                    text = stringResource(R.string.background_crop_reset),
                    onClick = actions.onReset,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = stringResource(R.string.background_crop_use_original),
                    onClick = actions.onUseOriginal,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(
                        if (state.saving) R.string.background_crop_saving
                        else R.string.background_crop_confirm,
                    ),
                    onClick = actions.onConfirm,
                    enabled = !state.saving && !state.loading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aefox.reyang.R
import aefox.reyang.ui.component.material.expressiveTopAppBarColors
import aefox.reyang.ui.theme.rememberAppContainerColor

/** Background crop (Material expressive mode): preview area plus ratio/rotate/apply actions. */
@Composable
fun BackgroundCropScreenMaterial(
    state: BackgroundCropUiState,
    actions: BackgroundCropActions,
    preview: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.background_crop_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = expressiveTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BackgroundCropRatio.entries.forEach { ratio ->
                    TextButton(onClick = { actions.onSelectRatio(ratio) }) {
                        Text(if (ratio == state.ratio) "✓ " + ratio.label() else ratio.label())
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = actions.onRotate) {
                    Text(stringResource(R.string.background_crop_rotate))
                }
                TextButton(onClick = actions.onReset) {
                    Text(stringResource(R.string.background_crop_reset))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = actions.onUseOriginal,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.background_crop_use_original))
                }
                FilledTonalButton(
                    onClick = actions.onConfirm,
                    enabled = !state.saving && !state.loading,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(
                            if (state.saving) R.string.background_crop_saving
                            else R.string.background_crop_confirm,
                        ),
                    )
                }
            }
        }
    }
}

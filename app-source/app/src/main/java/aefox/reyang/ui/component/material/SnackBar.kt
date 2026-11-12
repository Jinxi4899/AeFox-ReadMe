package aefox.reyang.ui.component.material

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aefox.reyang.ui.component.AeFoxIcon

@Composable
fun AeFoxSnackbar(data: SnackbarData) {
    val actionLabel = data.visuals.actionLabel
    Snackbar(
        modifier = Modifier.padding(horizontal = 12.dp),
        action = actionLabel?.let { label ->
            {
                TextButton(onClick = { data.performAction() }) { Text(label) }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AeFoxIcon(size = 28.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = data.visuals.message,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SnackBarHost(
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState,
    snackBar: @Composable (SnackbarData) -> Unit = { AeFoxSnackbar(it) },
) {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        backgroundContent = {},
        onDismiss = {
            hostState.currentSnackbarData?.dismiss()
        }
    ) {
        SnackbarHost(
            modifier = modifier,
            hostState = hostState,
            snackbar = snackBar,
        )
    }
    LaunchedEffect(hostState.currentSnackbarData) {
        if (hostState.currentSnackbarData == null) return@LaunchedEffect
        state.snapTo(SwipeToDismissBoxValue.Settled)
    }
}

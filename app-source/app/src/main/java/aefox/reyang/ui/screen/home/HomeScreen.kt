package aefox.reyang.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import aefox.reyang.core.system.ModuleActivation
import aefox.reyang.core.system.PermissionManager
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomePager(
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true
) {
    val viewModel = viewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionManager = remember(context) { PermissionManager(context) }
    val permissionState by permissionManager.state.collectAsStateWithLifecycle()

    // Module activation state (Root / module framework / module enabled / scope);
    // probing shells out to su, so it runs off the main thread.
    var activationActive by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun refreshActivation() {
        scope.launch(Dispatchers.IO) {
            val active = ModuleActivation.conditions(context).all { it.satisfied }
            withContext(Dispatchers.Main) { activationActive = active }
        }
    }
    fun refreshAll() {
        if (refreshing) return
        refreshing = true
        permissionManager.refresh()
        scope.launch(Dispatchers.IO) {
            val active = ModuleActivation.conditions(context).all { it.satisfied }
            withContext(Dispatchers.Main) {
                activationActive = active
                refreshing = false
            }
        }
    }

    val latestIsCurrentPage by rememberUpdatedState(isCurrentPage)
    val initialResumeHandled = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            viewModel.refresh()
        }
    }
    LifecycleResumeEffect(permissionManager) {
        permissionManager.refresh()
        refreshActivation()
        if (initialResumeHandled.value && latestIsCurrentPage) {
            viewModel.refresh()
        }
        initialResumeHandled.value = true
        onPauseOrDispose { }
    }

    val actions = HomeActions(
        onRefresh = { refreshAll() },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> HomePagerMiuix(
            state = uiState,
            permissionState = permissionState,
            activationActive = activationActive,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
            isRefreshing = refreshing,
        )

        UiMode.Material -> HomePagerMaterial(
            state = uiState,
            permissionState = permissionState,
            activationActive = activationActive,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
            isRefreshing = refreshing,
        )
    }
}

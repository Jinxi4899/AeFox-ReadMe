package aefox.reyang.ui.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import aefox.reyang.BuildConfig
import aefox.reyang.R
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.navigation3.LocalNavigator
import aefox.reyang.ui.theme.themeCustomText

@Composable
fun AboutScreen() {
    val navigator = LocalNavigator.current
    val state = AboutUiState(
        title = themeCustomText("page.about", stringResource(R.string.about)),
        appName = stringResource(R.string.app_name),
        versionName = BuildConfig.VERSION_DISPLAY,
    )
    val actions = AboutScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> AboutScreenMiuix(state, actions)
        UiMode.Material -> AboutScreenMaterial(state, actions)
    }
}

package aefox.reyang.ui.screen.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import aefox.reyang.R
import aefox.reyang.ui.component.material.SegmentedColumn
import aefox.reyang.ui.component.material.SegmentedListItem
import aefox.reyang.ui.component.material.expressiveTopAppBarColors
import aefox.reyang.ui.theme.rememberAppContainerColor
import aefox.reyang.ui.theme.SukiSuSuperuserNouns
import aefox.reyang.ui.theme.themeCustomLabel

@Composable
internal fun AppsPagerMaterial(
    entries: List<AppEntry>,
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        containerColor = rememberAppContainerColor(),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        themeCustomLabel(
                            "page.superuser",
                            stringResource(R.string.apps),
                            SukiSuSuperuserNouns,
                        )
                    )
                },
                colors = expressiveTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.apps_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            if (entries.isEmpty()) {
                EmptyStateMaterial()
            } else {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    content = buildList {
                        entries.forEach { entry ->
                            add {
                                SegmentedListItem(
                                    leadingContent = { AppIcon(entry.icon) },
                                    headlineContent = { Text(entry.name) },
                                    supportingContent = {
                                        Text(
                                            entry.versionName
                                                ?: stringResource(R.string.apps_not_installed)
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(bottomInnerPadding))
        }
    }
}

@Composable
private fun EmptyStateMaterial() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.apps_no_match),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

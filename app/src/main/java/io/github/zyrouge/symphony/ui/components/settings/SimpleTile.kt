package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSimpleTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = onClick
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            supportingContent = { subtitle?.let { it() } }
        )
    }
}

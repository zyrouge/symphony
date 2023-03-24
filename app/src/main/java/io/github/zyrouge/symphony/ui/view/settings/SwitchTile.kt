package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            onChange(!value)
        }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            trailingContent = {
                Switch(
                    checked = value,
                    onCheckedChange = {
                        onChange(!value)
                    }
                )
            }
        )
    }
}

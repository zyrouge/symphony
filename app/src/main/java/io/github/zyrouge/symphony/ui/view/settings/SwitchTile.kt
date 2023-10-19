package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: Boolean,
    onChange: (Boolean) -> Unit,
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

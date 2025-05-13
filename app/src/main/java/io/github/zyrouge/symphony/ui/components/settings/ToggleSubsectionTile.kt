package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ToggleSubsectionTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: Boolean,
    onClick: () -> Unit,
    onChange: (Boolean) -> Unit,
) {
    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            onClick()
        }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            trailingContent = {
                Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                    VerticalDivider(Modifier.padding(PaddingValues(10.dp, 5.dp)))
                    Switch(
                        checked = value,
                        onCheckedChange = {
                            onChange(!value)
                        }
                    )
                }
            }
        )
    }
}

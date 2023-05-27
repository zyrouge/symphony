package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object SettingsTileDefaults {
    @Composable
    fun cardColors() = CardDefaults.cardColors(containerColor = Color.Transparent)

    @Composable
    fun listItemColors() = ListItemDefaults.colors(containerColor = Color.Transparent)
}

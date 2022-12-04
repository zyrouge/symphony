package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

object SettingsTileDefaults {
    @Composable
    fun cardColors() = CardDefaults.cardColors(containerColor = Color.Transparent)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun listItemColors() = ListItemDefaults.colors(containerColor = Color.Transparent)
}

package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InfoTile(content: @Composable ColumnScope.() -> Unit) {
    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Icon(Icons.Outlined.Info, null)
            Spacer(Modifier.fillMaxWidth().height(10.dp))
            content()
        }
    }
}

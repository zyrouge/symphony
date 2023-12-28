package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun InformationDialog(
    context: ViewContext,
    content: @Composable (ColumnScope.() -> Unit),
    onDismissRequest: () -> Unit,
) {
    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.Details)
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(16.dp, 12.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                content()
            }
        }
    )
}

@Composable
fun InformationKeyValue(key: String, value: @Composable () -> Unit) {
    Column {
        Text(
            key,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
        ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
            value()
        }
    }
}

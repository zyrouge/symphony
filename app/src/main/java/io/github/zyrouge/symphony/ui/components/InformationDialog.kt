package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun InformationDialog(
    context: ViewContext,
    content: @Composable (ColumnScope.() -> Unit),
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    context.symphony.t.details,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier
                        .padding(20.dp, 0.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Column(
                    modifier = Modifier.padding(16.dp, 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun InformationKeyValue(key: String, value: String) {
    Column {
        Text(
            key,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

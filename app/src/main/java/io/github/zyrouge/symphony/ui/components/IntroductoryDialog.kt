package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun IntroductoryDialog(
    context: ViewContext,
    onDismissRequest: () -> Unit,
) {
    val checkForUpdates by context.symphony.settings.checkForUpdates.collectAsState()
    val showUpdateToast by context.symphony.settings.showUpdateToast.collectAsState()

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("\uD83D\uDC4B " + context.symphony.t.HelloThere)
        },
        content = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    context.symphony.t.IntroductoryMessage,
                    modifier = Modifier.padding(16.dp, 12.dp),
                )
                HorizontalDivider()
                Box(modifier = Modifier.height(8.dp))
                OptInTile(
                    content = { Text(context.symphony.t.CheckForUpdates) },
                    value = checkForUpdates,
                    onChange = { value ->
                        context.symphony.settings.setCheckForUpdates(value)
                    }
                )
                Box(modifier = Modifier.height(4.dp))
                OptInTile(
                    content = { Text(context.symphony.t.ShowUpdateToast) },
                    value = showUpdateToast,
                    onChange = { value ->
                        context.symphony.settings.setShowUpdateToast(value)
                    },
                    enabled = checkForUpdates,
                )
                Box(modifier = Modifier.height(8.dp))
            }
        }
    )
}

@Composable
private fun OptInTile(
    content: @Composable () -> Unit,
    value: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .run {
                when {
                    enabled -> clickable { onChange(!value) }
                    else -> alpha(0.7f)
                }
            }
            .padding(16.dp, 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            value -> Icon(
                Icons.Default.Check,
                null,
                tint = when {
                    enabled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(20.dp),
            )

            else -> Icon(
                Icons.Default.Close,
                null,
                tint = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        Box(modifier = Modifier.width(8.dp))
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            content()
        }
    }
}

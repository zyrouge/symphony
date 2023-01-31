package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun ConfirmationDialog(
    context: ViewContext,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    onResult: (Boolean) -> Unit,
) {
    Dialog(onDismissRequest = { onResult(false) }) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(20.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        title()
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Box(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 4.dp,
                    )
                ) {
                    description()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 0.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onResult(false) }) {
                        Text(context.symphony.t.no)
                    }
                    TextButton(onClick = { onResult(true) }) {
                        Text(context.symphony.t.yes)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

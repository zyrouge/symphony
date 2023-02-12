package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.utils.applyAll

object ScaffoldDialogDefaults {
    const val PreferredMaxHeight = 0.8f
}

@Composable
fun ScaffoldDialog(
    title: @Composable () -> Unit,
    titleLeading: (@Composable () -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
    contentHeight: Float? = null,
    onDismissRequest: () -> Unit,
) {
    val configuration = LocalConfiguration.current

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.applyAll {
                apply {
                    val maxHeight = (configuration.screenHeightDp * 0.9f).dp
                    when {
                        contentHeight != null -> height(maxHeight.times(contentHeight))
                        else -> requiredHeightIn(max = maxHeight)
                    }
                }
            },
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    titleLeading?.invoke()
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(20.dp, 0.dp)
                            .weight(1f)
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
                    titleTrailing?.invoke()
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                topBar?.invoke()
                Box(
                    modifier = Modifier.applyAll {
                        contentHeight?.let {
                            apply { weight(1f) }
                        }
                    }
                ) {
                    content()
                }
                actions?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 0.dp),
                    ) {
                        actions()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

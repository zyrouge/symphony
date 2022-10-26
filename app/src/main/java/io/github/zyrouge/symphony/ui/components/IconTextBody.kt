package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IconTextBody(icon: @Composable (Modifier) -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconTextBodyCompact(icon, content)
    }
}

@Composable
fun IconTextBodyCompact(icon: @Composable (Modifier) -> Unit, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon(Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)) {
            content()
        }
    }
}

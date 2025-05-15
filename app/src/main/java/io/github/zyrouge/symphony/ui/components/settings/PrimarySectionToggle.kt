package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimarySectionToggle(value: Boolean, onChanged: (Boolean) -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        Modifier.fillMaxWidth().padding(16.dp).clickable(interactionSource, null) { onChanged(!value) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            Modifier.padding(PaddingValues(20.dp, 10.dp)).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                content()
            }
            Spacer(Modifier.weight(1F))
            Switch(
                checked = value,
                onCheckedChange = onChanged,
                interactionSource = interactionSource,
            )
        }
    }
}

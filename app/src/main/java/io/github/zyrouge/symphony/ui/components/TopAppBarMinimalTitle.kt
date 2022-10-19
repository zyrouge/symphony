package io.github.zyrouge.symphony.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TopAppBarMinimalTitle(content: @Composable () -> Unit) {
    ProvideTextStyle(
        MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )
    ) {
        content()
    }
}

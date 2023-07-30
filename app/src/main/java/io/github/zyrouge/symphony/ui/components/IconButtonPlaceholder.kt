package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val IconButtonPlaceholderSize = 48.dp

@Composable
fun IconButtonPlaceholder() {
    Box(modifier = Modifier.size(IconButtonPlaceholderSize))
}

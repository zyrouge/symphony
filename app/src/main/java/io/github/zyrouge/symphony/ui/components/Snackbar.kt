package io.github.zyrouge.symphony.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable

@Composable
fun AdaptiveSnackbar(snackbarData: SnackbarData) = Snackbar(
    snackbarData = snackbarData,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    actionColor = MaterialTheme.colorScheme.primary,
    actionContentColor = MaterialTheme.colorScheme.primary,
    dismissActionContentColor = MaterialTheme.colorScheme.onSurface,
)

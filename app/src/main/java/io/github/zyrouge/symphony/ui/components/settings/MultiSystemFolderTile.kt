package io.github.zyrouge.symphony.ui.components.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.components.ScaffoldDialogDefaults
import io.github.zyrouge.symphony.ui.components.drawScrollBar
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.ActivityUtils

@Composable
fun SettingsMultiSystemFolderTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    initialValues: Set<Uri>,
    onChange: (Set<Uri>) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            showDialog = !showDialog
        }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            supportingContent = {
                Text(context.symphony.t.XFolders(initialValues.size.toString()))
            },
        )
    }

    if (showDialog) {
        val values = remember { initialValues.toMutableStateList() }
        val pickFolderLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri?.let { _ ->
                ActivityUtils.makePersistableReadableUri(context.symphony.applicationContext, uri)
                values.add(uri)
            }
        }

        // TODO: workaround for dialog resize bug
        //       https://issuetracker.google.com/issues/221643630
        key(values.size) {
            ScaffoldDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = title,
                contentHeight = ScaffoldDialogDefaults.PreferredMaxHeight,
                content = {
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .drawScrollBar(lazyListState),
                    ) {
                        itemsIndexed(values) { i, x ->
                            Card(
                                colors = SettingsTileDefaults.cardColors(),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 8.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(x.toString(), modifier = Modifier.weight(1f))
                                    IconButton(onClick = { values.removeAt(i) }) {
                                        Icon(Icons.Filled.Delete, null)
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            pickFolderLauncher.launch(null)
                        }
                    ) {
                        Text(context.symphony.t.AddFolder)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text(context.symphony.t.Cancel)
                    }
                    TextButton(
                        onClick = {
                            onChange(values.toSet())
                            showDialog = false
                        }
                    ) {
                        Text(context.symphony.t.Done)
                    }
                }
            )
        }
    }
}

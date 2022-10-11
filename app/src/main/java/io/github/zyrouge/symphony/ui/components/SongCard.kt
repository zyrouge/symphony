package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(context: ViewContext, song: Song, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        var showOptionsMenu by remember { mutableStateOf(false) }
        var showInfoDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 4.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    bitmap = song.getArtwork()
                        .asImageBitmap(),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.bodyMedium)
                    song.artistName?.let { artistName ->
                        Text(
                            artistName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.width(15.dp))
                IconButton(
                    onClick = { showOptionsMenu = true }
                ) {
                    Icon(Icons.Default.MoreVert, null)
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.Info, null)
                            },
                            text = {
                                Text(Symphony.t.details)
                            },
                            onClick = {
                                showOptionsMenu = false
                                showInfoDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showInfoDialog) {
            SongInformationDialog(
                context,
                song = song,
                onDismissRequest = {
                    showInfoDialog = false
                }
            )
        }
    }
}



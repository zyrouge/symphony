package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericGrooveCard(
    image: ImageRequest?,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    options: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(12.dp, 12.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                image?.let {
                    AsyncImage(
                        it,
                        null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        title()
                    }
                    subtitle?.let {
                        ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                            it()
                        }
                    }
                }
                Spacer(modifier = Modifier.width(15.dp))

                options?.let {
                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showOptionsMenu = !showOptionsMenu }
                    ) {
                        Icon(Icons.Default.MoreVert, null)
                        it(showOptionsMenu) {
                            showOptionsMenu = false
                        }
                    }
                }
            }
        }
    }
}

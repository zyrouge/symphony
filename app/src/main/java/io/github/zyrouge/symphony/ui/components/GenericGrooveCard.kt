package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    imageLabel: (@Composable () -> Unit)? = null,
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
                    Box {
                        AsyncImage(
                            it,
                            null,
                            modifier = Modifier
                                .size(45.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        )
                        imageLabel?.let { it ->
                            Box(
                                modifier = Modifier
                                    .offset(y = 8.dp)
                                    .align(Alignment.BottomCenter)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(3.dp, 0.dp)
                                ) {
                                    ProvideTextStyle(
                                        MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    ) { it() }
                                }
                            }
                        }
                    }
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
                        Icon(Icons.Filled.MoreVert, null)
                        it(showOptionsMenu) {
                            showOptionsMenu = false
                        }
                    }
                }
            }
        }
    }
}

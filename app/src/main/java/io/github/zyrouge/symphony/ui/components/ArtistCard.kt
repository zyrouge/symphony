package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistCard(context: ViewContext, artist: Artist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = {}
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    bitmap = artist.getArtwork()
                        .asImageBitmap(),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(artist.artistName, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

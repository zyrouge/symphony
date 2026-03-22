package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericGrooveArtworkGrid(images: List<ImageRequest>) {
    val imagesCount = images.size

    when {
        imagesCount == 0 -> {
            // TODO
        }

        images.size == 1 -> Box {
            AsyncImage(
                images.first(),
                null,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        }

        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            items(4) { i ->
                // TODO
                AsyncImage(
                    images[i],
                    null,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
    images?.let {
        Box {
            AsyncImage(
                it,
                null,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        }
    }
}

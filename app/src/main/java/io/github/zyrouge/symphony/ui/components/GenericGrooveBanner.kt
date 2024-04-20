package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.zyrouge.symphony.ui.helpers.ScreenOrientation

@Composable
fun GenericGrooveBanner(
    image: ImageRequest,
    options: @Composable (Boolean, () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val defaultHorizontalPadding = 20.dp
    BoxWithConstraints {
        AsyncImage(
            image,
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    when (ScreenOrientation.fromConfiguration(LocalConfiguration.current)) {
                        ScreenOrientation.PORTRAIT -> maxWidth.times(0.7f)
                        ScreenOrientation.LANDSCAPE -> maxWidth.times(0.25f)
                    }
                )
        )
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .padding(defaultHorizontalPadding, 32.dp, defaultHorizontalPadding, 12.dp)
                    .weight(1f)
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.headlineSmall
                        .copy(fontWeight = FontWeight.Bold)
                ) {
                    content()
                }
            }

            Box(modifier = Modifier.padding(4.dp)) {
                var showOptionsMenu by remember {
                    mutableStateOf(false)
                }
                IconButton(
                    onClick = {
                        showOptionsMenu = !showOptionsMenu
                    }
                ) {
                    Icon(Icons.Filled.MoreVert, null)
                    options(showOptionsMenu) {
                        showOptionsMenu = false
                    }
                }
            }
        }
    }
}

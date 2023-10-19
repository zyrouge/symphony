package io.github.zyrouge.symphony.ui.view.nowPlaying

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingAppBar(context: ViewContext) {
    CenterAlignedTopAppBar(
        title = {
            TopAppBarMinimalTitle {
                Text(context.symphony.t.NowPlaying)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        navigationIcon = {
            IconButton(
                onClick = {
                    context.navController.popBackStack()
                }
            ) {
                Icon(
                    Icons.Filled.ExpandMore,
                    null,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        actions = {
            IconButtonPlaceholder()
        },
    )
}

@Composable
fun NowPlayingLandscapeAppBar(context: ViewContext) {
    Row(
        modifier = Modifier.padding(defaultHorizontalPadding, 4.dp, 12.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopAppBarMinimalTitle(
            modifier = Modifier.weight(1f),
            fillMaxWidth = false,
        ) {
            Text(context.symphony.t.NowPlaying)
        }
        IconButton(
            onClick = {
                context.navController.popBackStack()
            }
        ) {
            Icon(
                Icons.Filled.ExpandMore,
                null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.LyricsText
import io.github.zyrouge.symphony.ui.components.TimedContentTextStyle
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NothingPlaying
import io.github.zyrouge.symphony.ui.view.nowPlaying.NowPlayingSeekBar
import io.github.zyrouge.symphony.ui.view.nowPlaying.NowPlayingTraditionalControls
import io.github.zyrouge.symphony.ui.view.nowPlaying.defaultHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsView(context: ViewContext) {
    BackHandler {
        context.navController.popBackStack()
    }

    NowPlayingWithData(context) { data ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
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
                    title = {
                        TopAppBarMinimalTitle {
                            Text(
                                context.symphony.t.Lyrics +
                                        (data?.song?.title?.let { " - $it" } ?: "")
                            )
                        }
                    },
                    actions = {
                        IconButtonPlaceholder()
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                )
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
            ) {
                when {
                    data != null -> Column {
                        Box(modifier = Modifier.weight(1f)) {
                            LyricsText(
                                context,
                                style = TimedContentTextStyle(
                                    highlighted = MaterialTheme.typography.titleMedium.copy(
                                        color = LocalContentColor.current,
                                    ),
                                    active = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    ),
                                    inactive = MaterialTheme.typography.titleMedium.copy(
                                        color = LocalContentColor.current.copy(alpha = 0.5f),
                                    ),
                                    spacing = 8.dp,
                                ),
                                padding = PaddingValues(
                                    horizontal = defaultHorizontalPadding,
                                    vertical = 12.dp,
                                ),
                            )
                        }
                        Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
                        NowPlayingSeekBar(context)
                        Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
                        NowPlayingTraditionalControls(context, data = data)
                        Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
                    }

                    else -> NothingPlaying(context)
                }
            }
        }
    }
}

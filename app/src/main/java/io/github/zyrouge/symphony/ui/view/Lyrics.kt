package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.LyricsText
import io.github.zyrouge.symphony.ui.components.TimedContentTextStyle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NothingPlaying
import io.github.zyrouge.symphony.ui.view.nowPlaying.defaultHorizontalPadding

@Composable
fun LyricsView(context: ViewContext) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val song by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val isViable by remember(song) {
        derivedStateOf { song != null }
    }

    BackHandler {
        context.navController.popBackStack()
    }

    when {
        isViable -> LyricsText(
            context,
            style = TimedContentTextStyle(
                highlighted = MaterialTheme.typography.titleMedium,
                active = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                inactive = MaterialTheme.typography.titleMedium.copy(
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                ),
            ),
            padding = PaddingValues(
                horizontal = defaultHorizontalPadding,
                vertical = 12.dp,
            ),
        )

        else -> NothingPlaying(context)
    }
}

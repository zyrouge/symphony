package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.AlbumArtistGrid
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumArtistsView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(
        context,
        isLoading = data.albumArtistsIsUpdating,
    ) {
        when {
            data.albumArtists.isNotEmpty() -> AlbumArtistGrid(
                context,
                data.albumArtists.toList(),
            )
            else -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.damnThisIsSoEmpty) }
            )
        }
    }
}

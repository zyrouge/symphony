package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun TreeView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
//    val disabledTreePaths by context.symphony.settings.lastDisabledTreePaths.flow.collectAsStateWithLifecycle()

//    LoaderScaffold(context, isLoading = isUpdating) {
//        SongTreeList(
//            context,
//            songIds = songIds,
//            songsCount = songsCount,
//            initialDisabled = disabledTreePaths.toList(),
//            onDisable = { paths ->
//                context.symphony.settings.lastDisabledTreePaths.setValue(paths.toSet())
//            },
//        )
//    }
}

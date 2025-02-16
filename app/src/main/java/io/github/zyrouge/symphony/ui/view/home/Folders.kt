package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun FoldersView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
    val sortBy by context.symphony.settings.lastUsedFoldersSortBy.flow.collectAsStateWithLifecycle()
    val sortReverse by context.symphony.settings.lastUsedFoldersSortReverse.flow.collectAsStateWithLifecycle()
    val folders by context.symphony.database.mediaTreeFolders.valuesAsFlow(sortBy, sortReverse)
        .collectAsStateWithLifecycle(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
//        AnimatedContent(
//            label = "folders-view-content",
//            targetState = currentFolder,
//            transitionSpec = {
//                val enter = when {
//                    targetState != null -> SlideTransition.slideUp.enterTransition()
//                    else -> FadeTransition.enterTransition()
//                }
//                enter.togetherWith(FadeTransition.exitTransition())
//            },
//        ) { folder ->
//            if (folder != null) {
//                val songIds by remember(folder) {
//                    derivedStateOf {
//                        folder.children.values.mapNotNull {
//                            when (it) {
//                                is SimpleFileSystem.File -> it.data as String
//                                else -> null
//                            }
//                        }
//                    }
//                }
//
//                Column {
//                    Column(
//                        modifier = Modifier.padding(
//                            start = defaultHorizontalPadding,
//                            end = defaultHorizontalPadding,
//                            top = 4.dp,
//                            bottom = 12.dp,
//                        ),
//                    ) {
//                        folder.parent?.let { parent ->
//                            Text(
//                                "${parent.fullPath}/",
//                                style = MaterialTheme.typography.bodyMedium.copy(
//                                    color = LocalContentColor.current.copy(alpha = 0.7f),
//                                ),
//                            )
//                        }
//                        Text(folder.name, style = MaterialTheme.typography.bodyLarge)
//                    }
//                    HorizontalDivider()
//                    SongList(context, songIds = songIds, songsCount = songIds.size)
//                }
//            } else {
//                FoldersGrid(
//                    context,
//                    folders = folders,
//                    onClick = {
//                        currentFolder = it
//                    }
//                )
//            }
//        }
//    }
    }
}

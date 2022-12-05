package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.github.zyrouge.symphony.ui.components.NowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigate
import io.github.zyrouge.symphony.ui.view.home.*
import io.github.zyrouge.symphony.utils.indexOfOrNull
import kotlinx.coroutines.launch

enum class HomePages(
    val label: (context: ViewContext) -> String,
    val selectedIcon: @Composable () -> ImageVector,
    val unselectedIcon: @Composable () -> ImageVector,
) {
    ForYou(
        label = { it.symphony.t.forYou },
        selectedIcon = { Icons.Filled.Face },
        unselectedIcon = { Icons.Outlined.Face }
    ),
    Songs(
        label = { it.symphony.t.songs },
        selectedIcon = { Icons.Filled.MusicNote },
        unselectedIcon = { Icons.Outlined.MusicNote }
    ),
    Artists(
        label = { it.symphony.t.artists },
        selectedIcon = { Icons.Filled.Group },
        unselectedIcon = { Icons.Outlined.Group }
    ),
    Albums(
        label = { it.symphony.t.albums },
        selectedIcon = { Icons.Filled.Album },
        unselectedIcon = { Icons.Outlined.Album }
    ),
    AlbumArtists(
        label = { it.symphony.t.albumArtists },
        selectedIcon = { Icons.Filled.SupervisorAccount },
        unselectedIcon = { Icons.Outlined.SupervisorAccount }
    ),
    Genres(
        label = { it.symphony.t.genres },
        selectedIcon = { Icons.Filled.Tune },
        unselectedIcon = { Icons.Outlined.Tune }
    ),
    Folders(
        label = { it.symphony.t.folders },
        selectedIcon = { Icons.Filled.Folder },
        unselectedIcon = { Icons.Outlined.Folder }
    );
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val tabs = context.symphony.settings.getHomeTabs().toList()
    val pageState = rememberPagerState(
        context.symphony.settings.getHomeLastTab()
            ?.let { value -> tabs.indexOfOrNull { it.name == value } }
            ?: 0
    )
    var currentPage by remember { mutableStateOf(tabs[pageState.currentPage]) }
    var showOptionsDropdown by remember { mutableStateOf(false) }
    val data = remember { HomeViewData(context.symphony) }

    DisposableEffect(LocalContext.current) {
        onDispose { data.dispose() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(
                        content = {
                            Icon(Icons.Default.Search, null)
                        },
                        onClick = {
                            context.navController.navigate(Routes.Search)
                        }
                    )
                },
                title = {
                    Crossfade(targetState = currentPage.label(context)) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            TopAppBarMinimalTitle { Text(it) }
                        }
                    }
                },
                actions = {
                    IconButton(
                        content = {
                            Icon(Icons.Default.MoreVert, null)
                            DropdownMenu(
                                expanded = showOptionsDropdown,
                                onDismissRequest = { showOptionsDropdown = false },
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            context.symphony.t.settings
                                        )
                                    },
                                    text = {
                                        Text(context.symphony.t.settings)
                                    },
                                    onClick = {
                                        showOptionsDropdown = false
                                        context.navController.navigate(Routes.Settings)
                                    }
                                )
                            }
                        },
                        onClick = {
                            showOptionsDropdown = !showOptionsDropdown
                        }
                    )
                }
            )
        },
        content = { contentPadding ->
            HorizontalPager(
                state = pageState,
                count = tabs.size,
                userScrollEnabled = false
            ) {
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    when (tabs[it]) {
                        HomePages.ForYou -> ForYouView(context, data)
                        HomePages.Songs -> SongsView(context, data)
                        HomePages.Albums -> AlbumsView(context, data)
                        HomePages.Artists -> ArtistsView(context, data)
                        HomePages.AlbumArtists -> AlbumArtistsView(context, data)
                        HomePages.Genres -> GenresView(context, data)
                        HomePages.Folders -> FoldersView(context, data)
                    }
                }
            }
        },
        bottomBar = {
            Column {
                NowPlayingBottomBar(context)
                NavigationBar {
                    tabs.map { page ->
                        val isSelected = currentPage == page
                        val label = page.label(context)
                        NavigationBarItem(
                            selected = isSelected,
                            alwaysShowLabel = false,
                            icon = {
                                Crossfade(targetState = isSelected) {
                                    Icon(
                                        if (it) page.selectedIcon() else page.unselectedIcon(),
                                        label
                                    )
                                }
                            },
                            label = {
                                Text(
                                    label,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                )
                            },
                            onClick = {
                                currentPage = page
                                coroutineScope.launch {
                                    pageState.animateScrollToPage(tabs.indexOf(page))
                                }
                                context.symphony.settings.setHomeLastTab(currentPage.name)
                            }
                        )
                    }
                }
            }
        }
    )
}

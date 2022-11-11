package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.github.zyrouge.symphony.ui.components.NowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigate
import io.github.zyrouge.symphony.ui.view.home.AlbumsView
import io.github.zyrouge.symphony.ui.view.home.ArtistsView
import io.github.zyrouge.symphony.ui.view.home.SongsView
import kotlinx.coroutines.launch

enum class HomePages(
    val label: (context: ViewContext) -> String,
    val selectedIcon: @Composable () -> ImageVector,
    val unselectedIcon: @Composable () -> ImageVector
) {
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
    );

    companion object {
        val mapped = values().mapIndexed { i, x ->
            i to x
        }.toMap()
        val size = mapped.size

        fun valueAt(index: Int) = mapped[index]!!
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val pageState = rememberPagerState(
        context.symphony.settings.getHomeLastTab()
            ?.let { value -> HomePages.values().find { it.name == value } }
            ?.ordinal ?: 0
    )
    var currentPage by remember { mutableStateOf(HomePages.valueAt(pageState.currentPage)) }
    var showOptionsDropdown by remember { mutableStateOf(false) }

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
                            modifier = Modifier
                                .fillMaxSize(),
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
                count = HomePages.size,
                userScrollEnabled = false
            ) {
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    when (HomePages.mapped[it]!!) {
                        HomePages.Songs -> SongsView(context)
                        HomePages.Albums -> AlbumsView(context)
                        HomePages.Artists -> ArtistsView(context)
                    }
                }
            }
        },
        bottomBar = {
            Column {
                NowPlayingBottomBar(context)
                NavigationBar {
                    HomePages.values().map { page ->
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
                            label = { Text(label) },
                            onClick = {
                                currentPage = page
                                coroutineScope.launch {
                                    pageState.animateScrollToPage(page.ordinal)
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

package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.IntroductoryDialog
import io.github.zyrouge.symphony.ui.components.NowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.*
import io.github.zyrouge.symphony.ui.view.home.*

enum class HomePages(
    val label: (context: ViewContext) -> String,
    val selectedIcon: @Composable () -> ImageVector,
    val unselectedIcon: @Composable () -> ImageVector,
) {
    ForYou(
        label = { it.symphony.t.ForYou },
        selectedIcon = { Icons.Filled.Face },
        unselectedIcon = { Icons.Outlined.Face }
    ),
    Songs(
        label = { it.symphony.t.Songs },
        selectedIcon = { Icons.Filled.MusicNote },
        unselectedIcon = { Icons.Outlined.MusicNote }
    ),
    Artists(
        label = { it.symphony.t.Artists },
        selectedIcon = { Icons.Filled.Group },
        unselectedIcon = { Icons.Outlined.Group }
    ),
    Albums(
        label = { it.symphony.t.Albums },
        selectedIcon = { Icons.Filled.Album },
        unselectedIcon = { Icons.Outlined.Album }
    ),
    AlbumArtists(
        label = { it.symphony.t.AlbumArtists },
        selectedIcon = { Icons.Filled.SupervisorAccount },
        unselectedIcon = { Icons.Outlined.SupervisorAccount }
    ),
    Genres(
        label = { it.symphony.t.Genres },
        selectedIcon = { Icons.Filled.Tune },
        unselectedIcon = { Icons.Outlined.Tune }
    ),
    Folders(
        label = { it.symphony.t.Folders },
        selectedIcon = { Icons.Filled.Folder },
        unselectedIcon = { Icons.Outlined.Folder }
    ),
    Playlists(
        label = { it.symphony.t.Playlists },
        selectedIcon = { Icons.Filled.QueueMusic },
        unselectedIcon = { Icons.Outlined.QueueMusic }
    ),
    Tree(
        label = { it.symphony.t.Tree },
        selectedIcon = { Icons.Filled.AccountTree },
        unselectedIcon = { Icons.Outlined.AccountTree }
    );
}

enum class HomePageBottomBarLabelVisibility {
    ALWAYS_VISIBLE,
    VISIBLE_WHEN_ACTIVE,
    INVISIBLE,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeView(context: ViewContext) {
    var showIntroductoryMessage by remember {
        mutableStateOf(
            !context.symphony.settings.getReadIntroductoryMessage()
        )
    }
    val tabs = context.symphony.settings.getHomeTabs().toList()
    val labelVisibility = context.symphony.settings.getHomePageBottomBarLabelVisibility()
    var currentPage by remember {
        mutableStateOf(context.symphony.settings.getHomeLastTab())
    }
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
                                            context.symphony.t.Settings
                                        )
                                    },
                                    text = {
                                        Text(context.symphony.t.Settings)
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
            AnimatedContent(
                targetState = currentPage,
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                transitionSpec = {
                    SlideTransition.slideUp.enterTransition()
                        .with(ScaleTransition.scaleDown.exitTransition())
                },
            ) { page ->
                when (page) {
                    HomePages.ForYou -> ForYouView(context)
                    HomePages.Songs -> SongsView(context)
                    HomePages.Albums -> AlbumsView(context)
                    HomePages.Artists -> ArtistsView(context)
                    HomePages.AlbumArtists -> AlbumArtistsView(context)
                    HomePages.Genres -> GenresView(context)
                    HomePages.Folders -> FoldersView(context)
                    HomePages.Playlists -> PlaylistsView(context)
                    HomePages.Tree -> TreeView(context)
                }
            }
        },
        bottomBar = {
            Column {
                NowPlayingBottomBar(context)
                NavigationBar {
                    Spacer(modifier = Modifier.width(2.dp))
                    tabs.map { page ->
                        val isSelected = currentPage == page
                        val label = page.label(context)
                        NavigationBarItem(
                            selected = isSelected,
                            alwaysShowLabel = labelVisibility == HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE,
                            icon = {
                                Crossfade(targetState = isSelected) {
                                    Icon(
                                        if (it) page.selectedIcon() else page.unselectedIcon(),
                                        label,
                                    )
                                }
                            },
                            label = when (labelVisibility) {
                                HomePageBottomBarLabelVisibility.INVISIBLE -> null
                                else -> ({
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                    )
                                })
                            },
                            onClick = {
                                currentPage = page
                                context.symphony.settings.setHomeLastTab(currentPage)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    )

    if (showIntroductoryMessage) {
        IntroductoryDialog(
            context,
            onDismissRequest = {
                showIntroductoryMessage = false
                context.symphony.settings.setReadIntroductoryMessage(true)
            },
        )
    }
}

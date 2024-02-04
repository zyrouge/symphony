package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.components.IntroductoryDialog
import io.github.zyrouge.symphony.ui.components.NowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.swipeable
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ScaleTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigate
import io.github.zyrouge.symphony.ui.view.home.AlbumArtistsView
import io.github.zyrouge.symphony.ui.view.home.AlbumsView
import io.github.zyrouge.symphony.ui.view.home.ArtistsView
import io.github.zyrouge.symphony.ui.view.home.BrowserView
import io.github.zyrouge.symphony.ui.view.home.FoldersView
import io.github.zyrouge.symphony.ui.view.home.ForYouView
import io.github.zyrouge.symphony.ui.view.home.GenresView
import io.github.zyrouge.symphony.ui.view.home.PlaylistsView
import io.github.zyrouge.symphony.ui.view.home.SongsView
import io.github.zyrouge.symphony.ui.view.home.TreeView
import kotlinx.coroutines.launch

enum class HomePages(
    val kind: GrooveKinds? = null,
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
        kind = GrooveKinds.SONG,
        label = { it.symphony.t.Songs },
        selectedIcon = { Icons.Filled.MusicNote },
        unselectedIcon = { Icons.Outlined.MusicNote }
    ),
    Artists(
        kind = GrooveKinds.ARTIST,
        label = { it.symphony.t.Artists },
        selectedIcon = { Icons.Filled.Group },
        unselectedIcon = { Icons.Outlined.Group }
    ),
    Albums(
        kind = GrooveKinds.ALBUM,
        label = { it.symphony.t.Albums },
        selectedIcon = { Icons.Filled.Album },
        unselectedIcon = { Icons.Outlined.Album }
    ),
    AlbumArtists(
        kind = GrooveKinds.ALBUM_ARTIST,
        label = { it.symphony.t.AlbumArtists },
        selectedIcon = { Icons.Filled.SupervisorAccount },
        unselectedIcon = { Icons.Outlined.SupervisorAccount }
    ),
    Genres(
        kind = GrooveKinds.GENRE,
        label = { it.symphony.t.Genres },
        selectedIcon = { Icons.Filled.Tune },
        unselectedIcon = { Icons.Outlined.Tune }
    ),
    Playlists(
        kind = GrooveKinds.PLAYLIST,
        label = { it.symphony.t.Playlists },
        selectedIcon = { Icons.AutoMirrored.Filled.QueueMusic },
        unselectedIcon = { Icons.AutoMirrored.Outlined.QueueMusic }
    ),
    Browser(
        label = { it.symphony.t.Browser },
        selectedIcon = { Icons.Filled.Folder },
        unselectedIcon = { Icons.Outlined.Folder }
    ),
    Folders(
        label = { it.symphony.t.Folders },
        selectedIcon = { Icons.Filled.FolderOpen },
        unselectedIcon = { Icons.Outlined.FolderOpen }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val readIntroductoryMessage by context.symphony.settings.readIntroductoryMessage.collectAsState()
    val tabs by context.symphony.settings.homeTabs.collectAsState()
    val labelVisibility by context.symphony.settings.homePageBottomBarLabelVisibility.collectAsState()
    val currentTab by context.symphony.settings.homeLastTab.collectAsState()
    var showOptionsDropdown by remember { mutableStateOf(false) }
    var showTabsSheet by remember { mutableStateOf(false) }

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
                            Icon(Icons.Filled.Search, null)
                        },
                        onClick = {
                            context.navController.navigate(Routes.Search.build(currentTab.kind))
                        }
                    )
                },
                title = {
                    Crossfade(
                        label = "home-title",
                        targetState = currentTab.label(context),
                    ) {
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
                            Icon(Icons.Filled.MoreVert, null)
                            DropdownMenu(
                                expanded = showOptionsDropdown,
                                onDismissRequest = { showOptionsDropdown = false },
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Refresh,
                                            context.symphony.t.Rescan,
                                        )
                                    },
                                    text = {
                                        Text(context.symphony.t.Rescan)
                                    },
                                    onClick = {
                                        showOptionsDropdown = false
                                        context.symphony.radio.stop()
                                        coroutineScope.launch {
                                            context.symphony.groove.refetch()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Settings,
                                            context.symphony.t.Settings,
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
                label = "home-content",
                targetState = currentTab,
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                transitionSpec = {
                    SlideTransition.slideUp.enterTransition()
                        .togetherWith(ScaleTransition.scaleDown.exitTransition())
                },
            ) { page ->
                when (page) {
                    HomePages.ForYou -> ForYouView(context)
                    HomePages.Songs -> SongsView(context)
                    HomePages.Albums -> AlbumsView(context)
                    HomePages.Artists -> ArtistsView(context)
                    HomePages.AlbumArtists -> AlbumArtistsView(context)
                    HomePages.Genres -> GenresView(context)
                    HomePages.Browser -> BrowserView(context)
                    HomePages.Folders -> FoldersView(context)
                    HomePages.Playlists -> PlaylistsView(context)
                    HomePages.Tree -> TreeView(context)
                }
            }
        },
        bottomBar = {
            Column {
                NowPlayingBottomBar(context, false)
                NavigationBar(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures {
                                showTabsSheet = true
                            }
                        }
                        .swipeable(onSwipeUp = {
                            showTabsSheet = true
                        })
                ) {
                    Spacer(modifier = Modifier.width(2.dp))
                    tabs.map { x ->
                        val isSelected = currentTab == x
                        val label = x.label(context)

                        NavigationBarItem(
                            selected = isSelected,
                            alwaysShowLabel = labelVisibility == HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE,
                            icon = {
                                Crossfade(
                                    label = "home-bottom-bar",
                                    targetState = isSelected,
                                ) {
                                    Icon(
                                        if (it) x.selectedIcon() else x.unselectedIcon(),
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
                                when {
                                    isSelected -> {
                                        showTabsSheet = true
                                    }

                                    else -> context.symphony.settings.setHomeLastTab(x)
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    )

    if (showTabsSheet) {
        val sheetState = rememberModalBottomSheetState()
        val orderedTabs = remember {
            setOf<HomePages>(*tabs.toTypedArray(), *HomePages.entries.toTypedArray())
        }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showTabsSheet = false
            },
        ) {
            LazyVerticalGrid(
                modifier = Modifier.padding(6.dp),
                columns = GridCells.Fixed(tabs.size),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(orderedTabs.toList(), key = { it.ordinal }) { x ->
                    val isSelected = x == currentTab
                    val label = x.label(context)

                    val containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                        else -> Color.Unspecified
                    }
                    val contentColor = when {
                        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> Color.Unspecified
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp, 0.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                context.symphony.settings.setHomeLastTab(x)
                                showTabsSheet = false
                            }
                            .background(containerColor)
                            .padding(0.dp, 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when {
                            isSelected -> Icon(x.selectedIcon(), label, tint = contentColor)
                            else -> Icon(x.unselectedIcon(), label)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall.copy(color = contentColor),
                            modifier = Modifier.padding(8.dp, 0.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (!readIntroductoryMessage) {
        IntroductoryDialog(
            context,
            onDismissRequest = {
                context.symphony.settings.setReadIntroductoryMessage(true)
            },
        )
    }
}

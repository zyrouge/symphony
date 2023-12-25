package io.github.zyrouge.symphony.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.RoutesParameters
import io.github.zyrouge.symphony.ui.helpers.ScaleTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.getRouteArgument
import io.github.zyrouge.symphony.ui.theme.SymphonyTheme

@Composable
fun BaseView(symphony: Symphony, activity: MainActivity) {
    val context = ViewContext(
        symphony = symphony,
        activity = activity,
        navController = rememberNavController(),
    )

    SymphonyTheme(context) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = context.navController,
                startDestination = Routes.Home.route
            ) {
                composable(
                    Routes.Home.route,
                    enterTransition = { FadeTransition.enterTransition() },
                ) {
                    HomeView(context)
                }
                composable(
                    Routes.NowPlaying.route,
                    enterTransition = { SlideTransition.slideUp.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                    popEnterTransition = { FadeTransition.enterTransition() },
                    popExitTransition = { SlideTransition.slideDown.exitTransition() },
                ) {
                    NowPlayingView(context)
                }
                composable(
                    Routes.Queue.route,
                    enterTransition = { SlideTransition.slideUp.enterTransition() },
                    exitTransition = { SlideTransition.slideDown.exitTransition() },
                ) {
                    QueueView(context)
                }
                composable(
                    Routes.Settings.route,
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    SettingsView(context)
                }
                composable(
                    Routes.Artist.route,
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    ArtistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.ArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Album.route,
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    AlbumView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.AlbumRouteAlbumId)
                            ?.toLongOrNull() ?: -1
                    )
                }
                composable(
                    Routes.Search.route,
                    enterTransition = { SlideTransition.slideDown.enterTransition() },
                    exitTransition = { SlideTransition.slideUp.exitTransition() },
                ) { backStackEntry ->
                    SearchView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.SearchRouteInitialChip)
                            ?.takeIf { it != "null" }
                            ?.let { GrooveKinds.valueOf(it) }
                    )
                }
                composable(
                    Routes.AlbumArtist.route,
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    AlbumArtistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.AlbumArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Genre.route,
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    GenreView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.GenreRouteGenre)
                            ?: ""
                    )
                }
                composable(
                    Routes.Playlist.route,
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    PlaylistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.PlaylistRoutePlaylistId)
                            ?: ""
                    )
                }
                composable(
                    Routes.Lyrics.route,
                    enterTransition = { SlideTransition.slideUp.enterTransition() },
                    exitTransition = { SlideTransition.slideDown.exitTransition() },
                ) {
                    LyricsView(context)
                }
            }
        }
    }
}

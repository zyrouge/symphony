package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.*
import io.github.zyrouge.symphony.ui.theme.SymphonyTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BaseView(symphony: Symphony, activity: MainActivity) {
    val context = ViewContext(
        symphony = symphony,
        activity = activity,
        navController = rememberAnimatedNavController()
    )

    SymphonyTheme(context) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AnimatedNavHost(
                navController = context.navController,
                startDestination = Routes.Home.route
            ) {
                composable(
                    Routes.Home.route,
                    popEnterTransition = FadeTransitions.FadeInEnterTransition,
                ) {
                    HomeView(context)
                }
                composable(
                    Routes.NowPlaying.route,
                    enterTransition = SlideTransitions.SlideUpEnterTransition,
                    exitTransition = FadeTransitions.FadeOutExitTransition,
                    popEnterTransition = FadeTransitions.FadeInEnterTransition,
                    popExitTransition = SlideTransitions.SlideDownExitTransition,
                ) {
                    NowPlayingView(context)
                }
                composable(
                    Routes.Queue.route,
                    enterTransition = SlideTransitions.SlideUpEnterTransition,
                    exitTransition = SlideTransitions.SlideDownExitTransition,
                ) {
                    QueueView(context)
                }
                composable(
                    Routes.Settings.route,
                    enterTransition = ScaleTransitions.ScaleUpEnterTransition,
                    exitTransition = ScaleTransitions.ScaleDownExitTransition,
                ) {
                    SettingsView(context)
                }
                composable(
                    Routes.Artist.route,
                    enterTransition = SlideTransitions.SlideFromRightEnterTransition,
                    exitTransition = SlideTransitions.SlideFromRightExitTransition,
                ) { backStackEntry ->
                    ArtistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.ArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Album.route,
                    enterTransition = SlideTransitions.SlideFromRightEnterTransition,
                    exitTransition = SlideTransitions.SlideFromRightExitTransition,
                ) { backStackEntry ->
                    AlbumView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.AlbumRouteAlbumId)
                            ?.toLongOrNull() ?: -1
                    )
                }
                composable(
                    Routes.Search.route,
                    enterTransition = SlideTransitions.SlideDownEnterTransition,
                    exitTransition = SlideTransitions.SlideUpExitTransition,
                ) {
                    SearchView(context)
                }
                composable(
                    Routes.AlbumArtist.route,
                    enterTransition = SlideTransitions.SlideFromRightEnterTransition,
                    exitTransition = SlideTransitions.SlideFromRightExitTransition,
                ) { backStackEntry ->
                    AlbumArtistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.AlbumArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Genre.route,
                    enterTransition = SlideTransitions.SlideFromRightEnterTransition,
                    exitTransition = SlideTransitions.SlideFromRightExitTransition,
                ) { backStackEntry ->
                    GenreView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.GenreRouteGenre)
                            ?: ""
                    )
                }
            }
        }
    }
}

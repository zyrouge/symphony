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
        navController = rememberAnimatedNavController(),
    )

    SymphonyTheme(context) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AnimatedNavHost(
                navController = context.navController,
                startDestination = Routes.Home.route
            ) {
                composable(
                    Routes.Home.route,
                    popEnterTransition = { FadeTransitions.fadeInEnterTransition() },
                ) {
                    HomeView(context)
                }
                composable(
                    Routes.NowPlaying.route,
                    enterTransition = { SlideTransitions.slideUp.enterTransition() },
                    exitTransition = { FadeTransitions.fadeOutExitTransition() },
                    popEnterTransition = { FadeTransitions.fadeInEnterTransition() },
                    popExitTransition = { SlideTransitions.slideDown.exitTransition() },
                ) {
                    NowPlayingView(context)
                }
                composable(
                    Routes.Queue.route,
                    enterTransition = { SlideTransitions.slideUp.enterTransition() },
                    exitTransition = { SlideTransitions.slideDown.exitTransition() },
                ) {
                    QueueView(context)
                }
                composable(
                    Routes.Settings.route,
                    enterTransition = { ScaleTransitions.scaleUpEnterTransition() },
                    exitTransition = { ScaleTransitions.scaleDownExitTransition() },
                ) {
                    SettingsView(context)
                }
                composable(
                    Routes.Artist.route,
                    enterTransition = { SlideTransitions.slideRight.enterTransition() },
                    exitTransition = { SlideTransitions.slideRight.exitTransition() },
                ) { backStackEntry ->
                    ArtistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.ArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Album.route,
                    enterTransition = { SlideTransitions.slideRight.enterTransition() },
                    exitTransition = { SlideTransitions.slideRight.exitTransition() },
                ) { backStackEntry ->
                    AlbumView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.AlbumRouteAlbumId)
                            ?.toLongOrNull() ?: -1
                    )
                }
                composable(
                    Routes.Search.route,
                    enterTransition = { SlideTransitions.slideDown.enterTransition() },
                    exitTransition = { SlideTransitions.slideUp.exitTransition() },
                ) {
                    SearchView(context)
                }
                composable(
                    Routes.AlbumArtist.route,
                    enterTransition = { SlideTransitions.slideRight.enterTransition() },
                    exitTransition = { SlideTransitions.slideRight.exitTransition() },
                ) { backStackEntry ->
                    AlbumArtistView(
                        context,
                        backStackEntry.getRouteArgument(RoutesParameters.AlbumArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Genre.route,
                    enterTransition = { SlideTransitions.slideRight.enterTransition() },
                    exitTransition = { SlideTransitions.slideRight.exitTransition() },
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

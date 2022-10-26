package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.RoutesParameters
import io.github.zyrouge.symphony.ui.helpers.Transitions
import io.github.zyrouge.symphony.ui.helpers.ViewContext
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
        Surface {
            AnimatedNavHost(
                navController = context.navController,
                startDestination = Routes.Home.route
            ) {
                composable(Routes.Home.route) {
                    HomeView(context)
                }
                composable(
                    Routes.NowPlaying.route,
                    enterTransition = Transitions.SlideUpEnterTransition,
                    exitTransition = Transitions.SlideDownExitTransition
                ) {
                    NowPlayingView(context)
                }
                composable(
                    Routes.Queue.route,
                    enterTransition = Transitions.SlideUpEnterTransition,
                    exitTransition = Transitions.SlideDownExitTransition
                ) {
                    QueueView(context)
                }
                composable(
                    Routes.Settings.route,
                    enterTransition = Transitions.ScaleUpEnterTransition,
                    exitTransition = Transitions.ScaleDownExitTransition
                ) {
                    SettingsView(context)
                }
                composable(
                    Routes.Artist.route,
                    enterTransition = Transitions.SlideFromRightEnterTransition,
                    exitTransition = Transitions.SlideFromRightExitTransition
                ) { backStackEntry ->
                    ArtistView(
                        context,
                        backStackEntry.arguments?.getString(RoutesParameters.ArtistRouteArtistName)
                            ?: ""
                    )
                }
                composable(
                    Routes.Album.route,
                    enterTransition = Transitions.SlideFromRightEnterTransition,
                    exitTransition = Transitions.SlideFromRightExitTransition
                ) { backStackEntry ->
                    AlbumView(
                        context,
                        backStackEntry.arguments?.getString(RoutesParameters.AlbumRouteAlbumId)
                            ?.toLongOrNull() ?: -1
                    )
                }
                composable(
                    Routes.Search.route,
                    enterTransition = Transitions.ScaleUpEnterTransition,
                    exitTransition = Transitions.ScaleDownExitTransition
                ) {
                    SearchView(context)
                }
            }
        }
    }
}

package io.github.zyrouge.symphony.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.ScaleTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.SymphonyTheme
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsView
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsView
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.HomePageSettingsView
import io.github.zyrouge.symphony.ui.view.settings.HomePageSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.MiniPlayerSettingsView
import io.github.zyrouge.symphony.ui.view.settings.MiniPlayerSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.NowPlayingSettingsView
import io.github.zyrouge.symphony.ui.view.settings.NowPlayingSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsView
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.UpdateSettingsView
import io.github.zyrouge.symphony.ui.view.settings.UpdateSettingsViewRoute
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer

@Composable
fun BaseView(symphony: Symphony, activity: MainActivity) {
    val navController = rememberNavController()
    val context = remember {
        ViewContext(
            symphony = symphony,
            activity = activity,
            navController = navController,
        )
    }

    SymphonyTheme(context) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = HomeViewRoute,
            ) {
                composable<HomeViewRoute>(
                    popEnterTransition = {
                        when {
                            initialState.destination.isRoute<NowPlayingViewRoute>() -> FadeTransition.enterTransition()
                            initialState.destination.isRoute<SearchViewRoute>() -> FadeTransition.enterTransition()
                            else -> SlideTransition.slideRight.enterTransition()
                        }
                    },
                    popExitTransition = { SlideTransition.slideRight.exitTransition() },
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = {
                        when {
                            targetState.destination.isRoute<NowPlayingViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                            targetState.destination.isRoute<SearchViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                            else -> SlideTransition.slideLeft.exitTransition()
                        }
                    },
                ) {
                    HomeView(context)
                }
                composable<NowPlayingViewRoute>(
                    enterTransition = { SlideTransition.slideUp.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                    popEnterTransition = { FadeTransition.enterTransition() },
                    popExitTransition = { SlideTransition.slideDown.exitTransition() },
                ) {
                    NowPlayingView(context)
                }
                composable<QueueViewRoute>(
                    enterTransition = { SlideTransition.slideUp.enterTransition() },
                    exitTransition = { SlideTransition.slideDown.exitTransition() },
                ) {
                    QueueView(context)
                }
                composable<ArtistViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) { backStackEntry ->
                    ArtistView(context, backStackEntry.toRoute())
                }
                composable<AlbumViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) { backStackEntry ->
                    AlbumView(context, backStackEntry.toRoute())
                }
                composable<SearchViewRoute>(
                    enterTransition = { SlideTransition.slideDown.enterTransition() },
                    exitTransition = { SlideTransition.slideUp.exitTransition() },
                ) { backStackEntry ->
                    SearchView(context, backStackEntry.toRoute())
                }
                composable<AlbumArtistViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) { backStackEntry ->
                    AlbumArtistView(context, backStackEntry.toRoute())
                }
                composable<GenreViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) { backStackEntry ->
                    GenreView(context, backStackEntry.toRoute())
                }
                composable<PlaylistViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) { backStackEntry ->
                    PlaylistView(context, backStackEntry.toRoute())
                }
                composable<LyricsViewRoute>(
                    enterTransition = { SlideTransition.slideUp.enterTransition() },
                    exitTransition = { SlideTransition.slideDown.exitTransition() },
                ) {
                    LyricsView(context)
                }
                composable<SettingsViewRoute>(
                    popEnterTransition = { SlideTransition.slideRight.enterTransition() },
                    popExitTransition = { SlideTransition.slideRight.exitTransition() },
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideLeft.exitTransition() },
                ) { backStackEntry ->
                    SettingsView(context, backStackEntry.toRoute())
                }
                composable<AppearanceSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) {
                    AppearanceSettingsView(context)
                }
                composable<GrooveSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) { backStackEntry ->
                    GrooveSettingsView(context, backStackEntry.toRoute())
                }
                composable<HomePageSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) {
                    HomePageSettingsView(context)
                }
                composable<MiniPlayerSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) {
                    MiniPlayerSettingsView(context)
                }
                composable<NowPlayingSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) {
                    NowPlayingSettingsView(context)
                }
                composable<PlayerSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) {
                    PlayerSettingsView(context)
                }
                composable<UpdateSettingsViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { SlideTransition.slideRight.exitTransition() },
                ) {
                    UpdateSettingsView(context)
                }
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> NavDestination.isRoute() =
    route?.contains(serializer<T>().descriptor.serialName) == true

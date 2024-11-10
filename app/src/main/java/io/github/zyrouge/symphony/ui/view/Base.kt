package io.github.zyrouge.symphony.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
                    enterTransition = { FadeTransition.enterTransition() },
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
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    ArtistView(context, backStackEntry.toRoute())
                }
                composable<AlbumViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
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
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    AlbumArtistView(context, backStackEntry.toRoute())
                }
                composable<GenreViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
                ) { backStackEntry ->
                    GenreView(context, backStackEntry.toRoute())
                }
                composable<PlaylistViewRoute>(
                    enterTransition = { SlideTransition.slideLeft.enterTransition() },
                    exitTransition = { FadeTransition.exitTransition() },
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
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) { backStackEntry ->
                    SettingsView(context, backStackEntry.toRoute())
                }
                composable<AppearanceSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    AppearanceSettingsView(context)
                }
                composable<GrooveSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) { backStackEntry ->
                    GrooveSettingsView(context, backStackEntry.toRoute())
                }
                composable<HomePageSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    HomePageSettingsView(context)
                }
                composable<MiniPlayerSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    MiniPlayerSettingsView(context)
                }
                composable<NowPlayingSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    NowPlayingSettingsView(context)
                }
                composable<PlayerSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    PlayerSettingsView(context)
                }
                composable<UpdateSettingsViewRoute>(
                    enterTransition = { ScaleTransition.scaleDown.enterTransition() },
                    exitTransition = { ScaleTransition.scaleUp.exitTransition() },
                ) {
                    UpdateSettingsView(context)
                }
            }
        }
    }
}

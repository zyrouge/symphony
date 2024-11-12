package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
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
                baseComposable<HomeViewRoute> {
                    HomeView(context)
                }
                baseComposable<NowPlayingViewRoute> {
                    NowPlayingView(context)
                }
                baseComposable<QueueViewRoute> {
                    QueueView(context)
                }
                baseComposable<ArtistViewRoute> {
                    ArtistView(context, it.toRoute())
                }
                baseComposable<AlbumViewRoute> {
                    AlbumView(context, it.toRoute())
                }
                baseComposable<SearchViewRoute> {
                    SearchView(context, it.toRoute())
                }
                baseComposable<AlbumArtistViewRoute> {
                    AlbumArtistView(context, it.toRoute())
                }
                baseComposable<GenreViewRoute> {
                    GenreView(context, it.toRoute())
                }
                baseComposable<PlaylistViewRoute> {
                    PlaylistView(context, it.toRoute())
                }
                baseComposable<LyricsViewRoute> {
                    LyricsView(context)
                }
                baseComposable<SettingsViewRoute> {
                    SettingsView(context, it.toRoute())
                }
                baseComposable<AppearanceSettingsViewRoute> {
                    AppearanceSettingsView(context)
                }
                baseComposable<GrooveSettingsViewRoute> {
                    GrooveSettingsView(context, it.toRoute())
                }
                baseComposable<HomePageSettingsViewRoute> {
                    HomePageSettingsView(context)
                }
                baseComposable<MiniPlayerSettingsViewRoute> {
                    MiniPlayerSettingsView(context)
                }
                baseComposable<NowPlayingSettingsViewRoute> {
                    NowPlayingSettingsView(context)
                }
                baseComposable<PlayerSettingsViewRoute> {
                    PlayerSettingsView(context)
                }
                baseComposable<UpdateSettingsViewRoute> {
                    UpdateSettingsView(context)
                }
            }
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.baseComposable(
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit),
) {
    composable<T>(
        popEnterTransition = {
            when {
                isInitialRoute<SearchViewRoute>() -> ScaleTransition.scaleUp.enterTransition()
                isInitialRoute<NowPlayingViewRoute>() -> ScaleTransition.scaleUp.enterTransition()
                isInitialRoute<QueueViewRoute>() -> ScaleTransition.scaleUp.enterTransition()
                isInitialRoute<LyricsViewRoute>() -> ScaleTransition.scaleUp.enterTransition()
                else -> SlideTransition.slideRight.enterTransition()
            }
        },
        popExitTransition = {
            when {
                isInitialRoute<SearchViewRoute>() -> SlideTransition.slideUp.exitTransition()
                isInitialRoute<NowPlayingViewRoute>() -> SlideTransition.slideDown.exitTransition()
                isInitialRoute<QueueViewRoute>() -> SlideTransition.slideDown.exitTransition()
                isInitialRoute<LyricsViewRoute>() -> SlideTransition.slideDown.exitTransition()
                else -> SlideTransition.slideRight.exitTransition()
            }
        },
        enterTransition = {
            when {
                isTargetRoute<SearchViewRoute>() -> SlideTransition.slideDown.enterTransition()
                isTargetRoute<NowPlayingViewRoute>() -> SlideTransition.slideUp.enterTransition()
                isTargetRoute<QueueViewRoute>() -> SlideTransition.slideUp.enterTransition()
                isTargetRoute<LyricsViewRoute>() -> SlideTransition.slideUp.enterTransition()
                else -> SlideTransition.slideLeft.enterTransition()
            }
        },
        exitTransition = {
            when {
                isTargetRoute<SearchViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                isTargetRoute<NowPlayingViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                isTargetRoute<QueueViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                isTargetRoute<LyricsViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                else -> SlideTransition.slideLeft.exitTransition()
            }
        },
    ) {
        content(it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> NavDestination.isRoute() =
    route?.contains(serializer<T>().descriptor.serialName) == true

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> AnimatedContentTransitionScope<NavBackStackEntry>.isInitialRoute() =
    initialState.destination.isRoute<T>()

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> AnimatedContentTransitionScope<NavBackStackEntry>.isTargetRoute() =
    targetState.destination.isRoute<T>()

package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import io.github.zyrouge.symphony.ui.theme.SymphonyTheme
import io.github.zyrouge.symphony.ui.view.helpers.Routes
import io.github.zyrouge.symphony.ui.view.helpers.Transitions
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BaseView() {
    val context = ViewContext(
        navController = rememberAnimatedNavController()
    )

    SymphonyTheme {
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
        }
    }
}

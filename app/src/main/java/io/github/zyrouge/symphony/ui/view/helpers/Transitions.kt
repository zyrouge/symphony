package io.github.zyrouge.symphony.ui.view.helpers

import androidx.compose.animation.*
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalAnimationApi::class)
private typealias EnterTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)

@OptIn(ExperimentalAnimationApi::class)
private typealias ExitTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)

@OptIn(ExperimentalAnimationApi::class)
object Transitions {
    val SlideUpEnterTransition: EnterTransitionFn = {
        slideIn {
            IntOffset(0, it.height / 2)
        } + fadeIn()
    }

    val SlideDownExitTransition: ExitTransitionFn = {
        slideOut {
            IntOffset(0, it.height / 2)
        } + fadeOut()
    }
}
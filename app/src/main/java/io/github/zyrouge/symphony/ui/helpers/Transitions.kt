@file:OptIn(ExperimentalAnimationApi::class)

package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.animation.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry

private typealias EnterTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)
private typealias ExitTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)

object Transitions {
    val SlideUpEnterTransition: EnterTransitionFn = {
        slideIn {
            calculateSlideTransitionOffset(it)
        } + fadeIn()
    }
    val SlideDownExitTransition: ExitTransitionFn = {
        slideOut {
            calculateSlideTransitionOffset(it)
        } + fadeOut()
    }

    private fun calculateSlideTransitionOffset(size: IntSize) = IntOffset(0, size.height / 2)

    private const val ScaleTransitionInitialScale = 0.7f
    val ScaleUpEnterTransition: EnterTransitionFn = {
        scaleIn(initialScale = ScaleTransitionInitialScale) + fadeIn()
    }
    val ScaleDownExitTransition: ExitTransitionFn = {
        scaleOut(targetScale = ScaleTransitionInitialScale) + fadeOut()
    }
}
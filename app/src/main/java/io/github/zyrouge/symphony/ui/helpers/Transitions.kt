@file:OptIn(ExperimentalAnimationApi::class)

package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.animation.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry

private typealias EnterTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)
private typealias ExitTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)

object Transitions {
    private fun calculateSlideTransitionOffset(size: IntSize) = IntOffset(0, size.height / 2)
    val SlideUpEnterTransition: EnterTransitionFn = {
        slideIn { calculateSlideTransitionOffset(it) } + fadeIn()
    }
    val SlideDownExitTransition: ExitTransitionFn = {
        slideOut { calculateSlideTransitionOffset(it) } + fadeOut()
    }


    private const val ScaleTransitionInitialScale = 0.7f
    val ScaleUpEnterTransition: EnterTransitionFn = {
        scaleIn(initialScale = ScaleTransitionInitialScale) + fadeIn()
    }
    val ScaleDownExitTransition: ExitTransitionFn = {
        scaleOut(targetScale = ScaleTransitionInitialScale) + fadeOut()
    }

    private fun calculateSlideFromRightOffset(size: IntSize) =
        IntOffset((0.2 * size.width).toInt(), 0)

    val SlideFromRightEnterTransition: EnterTransitionFn = {
        slideIn { calculateSlideFromRightOffset(it) } + fadeIn()
    }
    val SlideFromRightExitTransition: ExitTransitionFn = {
        slideOut { calculateSlideFromRightOffset(it) } + fadeOut()
    }
}
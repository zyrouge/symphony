@file:OptIn(ExperimentalAnimationApi::class)

package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry

private typealias EnterTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)
private typealias ExitTransitionFn = (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)

sealed class TransitionDurations(val milliseconds: Int) {
    object Normal : TransitionDurations(300)

    fun <T> asTween() = tween<T>(milliseconds)
}

object ScaleTransitions {
    val ScaleUpEnterTransition: EnterTransitionFn = {
        scaleIn(
            animationSpec = constructAnimationSpec(),
            initialScale = ScaleTransitionInitialScale
        ) + fadeIn()
    }
    val ScaleDownExitTransition: ExitTransitionFn = {
        scaleOut(
            animationSpec = constructAnimationSpec(),
            targetScale = ScaleTransitionInitialScale
        ) + fadeOut()
    }

    private const val ScaleTransitionInitialScale = 0.85f
    private fun <T> constructAnimationSpec() = TransitionDurations.Normal.asTween<T>()
}

object SlideTransitions {
    val SlideUpEnterTransition: EnterTransitionFn = {
        slideIn(constructAnimationSpec()) { calculateSlideDownOffset(it) } + fadeIn()
    }
    val SlideUpExitTransition: ExitTransitionFn = {
        slideOut(constructAnimationSpec()) { calculateSlideUpOffset(it) } + fadeOut()
    }
    val SlideDownEnterTransition: EnterTransitionFn = {
        slideIn(constructAnimationSpec()) { calculateSlideUpOffset(it) } + fadeIn()
    }
    val SlideDownExitTransition: ExitTransitionFn = {
        slideOut(constructAnimationSpec()) { calculateSlideDownOffset(it) } + fadeOut()
    }
    val SlideFromRightEnterTransition: EnterTransitionFn = {
        slideIn(constructAnimationSpec()) { calculateSlideRightOffset(it) } + fadeIn()
    }
    val SlideFromRightExitTransition: ExitTransitionFn = {
        slideOut(constructAnimationSpec()) { calculateSlideRightOffset(it) } + fadeOut()
    }

    private fun <T> constructAnimationSpec() = TransitionDurations.Normal.asTween<T>()
    private fun calculateSlideUpOffset(size: IntSize) = IntOffset(0, -size.height / 4)
    private fun calculateSlideDownOffset(size: IntSize) = IntOffset(0, size.height / 4)
    private fun calculateSlideRightOffset(size: IntSize) =
        IntOffset((0.2 * size.width).toInt(), 0)
}

object FadeTransitions {
    val FadeInEnterTransition: EnterTransitionFn = { fadeIn(constructAnimationSpec()) }
    val FadeOutExitTransition: ExitTransitionFn = { fadeOut(constructAnimationSpec()) }

    private fun <T> constructAnimationSpec() = TransitionDurations.Normal.asTween<T>()
}

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
    object Slow : TransitionDurations(500)

    fun <T> asTween() = tween<T>(milliseconds)
}

object ScaleTransitions {
    val ScaleUpEnterTransition: EnterTransitionFn = {
        scaleIn(
            animationSpec = constructAnimationSpec(),
            initialScale = ShrinkScale
        ) + fadeIn()
    }
    val ScaleUpExitTransition: ExitTransitionFn = {
        scaleOut(
            animationSpec = constructAnimationSpec(),
            targetScale = ExpandScale
        ) + fadeOut()
    }
    val ScaleDownExitTransition: ExitTransitionFn = {
        scaleOut(
            animationSpec = constructAnimationSpec(),
            targetScale = ShrinkScale
        ) + fadeOut()
    }

    private const val ShrinkScale = 0.95f
    private const val ExpandScale = 1.05f
    private fun <T> constructAnimationSpec() = TransitionDurations.Slow.asTween<T>()
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
    private fun calculateOffset(size: Int) = (0.1 * size).toInt()
    private fun calculateSlideUpOffset(size: IntSize) = IntOffset(0, calculateOffset(-size.height))
    private fun calculateSlideDownOffset(size: IntSize) = IntOffset(0, calculateOffset(size.height))
    private fun calculateSlideRightOffset(size: IntSize) = IntOffset(calculateOffset(size.width), 0)
}

object FadeTransitions {
    val FadeInEnterTransition: EnterTransitionFn = { fadeIn(constructAnimationSpec()) }
    val FadeOutExitTransition: ExitTransitionFn = { fadeOut(constructAnimationSpec()) }

    private fun <T> constructAnimationSpec() = TransitionDurations.Normal.asTween<T>()
}

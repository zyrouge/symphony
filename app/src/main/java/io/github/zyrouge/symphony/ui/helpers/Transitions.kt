@file:OptIn(ExperimentalAnimationApi::class)

package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

sealed class TransitionDurations(val milliseconds: Int) {
    object Normal : TransitionDurations(300)
    object Slow : TransitionDurations(500)

    fun <T> asTween(delayMillis: Int = 0) =
        tween<T>(milliseconds, delayMillis)
}

object ScaleTransitions {
    private const val ShrinkScale = 0.95f
    private const val ExpandScale = 1.05f

    fun scaleUpEnterTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        scaleIn(animationSpec = animationSpec, initialScale = ShrinkScale) + fadeIn()

    fun scaleUpExitTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        scaleOut(animationSpec = animationSpec, targetScale = ExpandScale) + fadeOut()

    fun scaleDownExitTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        scaleOut(animationSpec = animationSpec, targetScale = ShrinkScale) + fadeOut()

    private fun <T> defaultAnimationSpec() = TransitionDurations.Slow.asTween<T>()
}

data class SlideTransitions(val calculate: (IntSize) -> IntOffset) {
    fun enterTransition(
        animationSpec: FiniteAnimationSpec<IntOffset> = defaultAnimationSpec(),
    ) = slideIn(animationSpec) { calculate(it) } + fadeIn()

    fun exitTransition(
        animationSpec: FiniteAnimationSpec<IntOffset> = defaultAnimationSpec(),
    ) = slideOut(animationSpec) { calculate(it) } + fadeOut()

    companion object {
        val slideUp = SlideTransitions { IntOffset(0, calculateOffset(-it.height)) }
        val slideDown = SlideTransitions { IntOffset(0, calculateOffset(it.height)) }
        val slideLeft = SlideTransitions { IntOffset(calculateOffset(-it.width), 0) }
        val slideRight = SlideTransitions { IntOffset(calculateOffset(it.width), 0) }

        private fun calculateOffset(size: Int) = (0.1 * size).toInt()
        private fun <T> defaultAnimationSpec() = TransitionDurations.Normal.asTween<T>()
    }
}

object FadeTransitions {
    fun fadeInEnterTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        fadeIn(animationSpec)

    fun fadeOutExitTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        fadeOut(animationSpec)

    private fun <T> defaultAnimationSpec() = TransitionDurations.Normal.asTween<T>()
}

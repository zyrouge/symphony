package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

sealed class TransitionDurations(val milliseconds: Int) {
    data object Fast : TransitionDurations(200)
    data object Normal : TransitionDurations(400)
    data object Slow : TransitionDurations(600)

    fun <T> asTween(delayMillis: Int = 0, easing: Easing = FastOutSlowInEasing) =
        tween<T>(milliseconds, delayMillis, easing)
}

class ScaleTransition(val enterScale: Float, val exitScale: Float) {
    fun enterTransition(
        fadeAnimationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
        scaleAnimationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
    ) = fadeIn(fadeAnimationSpec) + scaleIn(scaleAnimationSpec, enterScale)

    fun exitTransition(
        fadeAnimationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
        scaleAnimationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
    ) = fadeOut(fadeAnimationSpec) + scaleOut(scaleAnimationSpec, exitScale)

    companion object {
        private const val ShrinkScale = 0.95f
        private const val ExpandScale = 1.05f

        val scaleUp = ScaleTransition(ShrinkScale, ExpandScale)
        val scaleDown = ScaleTransition(ExpandScale, ShrinkScale)

        private fun <T> defaultAnimationSpec() = TransitionDurations.Slow.asTween<T>()
    }
}

private typealias CalcOffsetFn = (IntSize) -> IntOffset

class SlideTransition(val enterOffset: CalcOffsetFn, val exitOffset: CalcOffsetFn) {
    fun enterTransition(
        fadeAnimationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
        slideAnimationSpec: FiniteAnimationSpec<IntOffset> = defaultAnimationSpec(),
    ) = fadeIn(fadeAnimationSpec) + slideIn(slideAnimationSpec) { enterOffset(it) }

    fun exitTransition(
        fadeAnimationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
        slideAnimationSpec: FiniteAnimationSpec<IntOffset> = defaultAnimationSpec(),
    ) = slideOut(slideAnimationSpec) { exitOffset(it) } + fadeOut(fadeAnimationSpec)

    companion object {
        private val slideUpOffset: CalcOffsetFn = { IntOffset(0, calculateOffset(-it.height)) }
        private val slideDownOffset: CalcOffsetFn = { IntOffset(0, calculateOffset(it.height)) }
        private val slideLeftOffset: CalcOffsetFn = { IntOffset(calculateOffset(-it.width), 0) }
        private val slideRightOffset: CalcOffsetFn = { IntOffset(calculateOffset(it.width), 0) }

        val slideUp = SlideTransition(slideDownOffset, slideUpOffset)
        val slideDown = SlideTransition(slideUpOffset, slideDownOffset)
        val slideLeft = SlideTransition(slideRightOffset, slideLeftOffset)
        val slideRight = SlideTransition(slideLeftOffset, slideRightOffset)

        private fun calculateOffset(size: Int) = (0.1 * size).toInt()
        private fun <T> defaultAnimationSpec() = TransitionDurations.Normal.asTween<T>()
    }
}

object FadeTransition {
    fun enterTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        fadeIn(animationSpec)

    fun exitTransition(animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec()) =
        fadeOut(animationSpec)

    private fun <T> defaultAnimationSpec() = TransitionDurations.Normal.asTween<T>()
}

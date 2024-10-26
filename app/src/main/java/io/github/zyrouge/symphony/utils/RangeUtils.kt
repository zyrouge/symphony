package io.github.zyrouge.symphony.utils

object RangeUtils {
    fun calculateGap(range: ClosedFloatingPointRange<Float>) = range.endInclusive - range.start

    fun calculateRatioFromValue(value: Float, range: ClosedFloatingPointRange<Float>) =
        (value - range.start) / calculateGap(range)

    fun calculateValueFromRatio(ratio: Float, range: ClosedFloatingPointRange<Float>) =
        (ratio * calculateGap(range)) + range.start
}

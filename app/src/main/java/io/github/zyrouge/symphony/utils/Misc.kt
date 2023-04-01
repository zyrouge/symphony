package io.github.zyrouge.symphony.utils

class SimpleTracker(
    val minValue: Int = 0,
    val maxValue: Int = 100,
    val initialValue: Int = minValue,
) {
    var value: Int = initialValue

    fun bump(): Int {
        val nValue = value + 1
        value = if (nValue > maxValue) minValue else nValue
        return value
    }

    fun matches(to: Int) = value == to
}

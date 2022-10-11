package io.github.zyrouge.symphony.utils

import java.util.concurrent.TimeUnit

object DurationFormatter {
    fun formatAsMS(ms: Int): String {
        val msLong = ms.toLong()
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(msLong) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(msLong) % TimeUnit.MINUTES.toSeconds(1)
        )
    }
}
package io.github.zyrouge.symphony.utils

import java.util.concurrent.TimeUnit

object DurationFormatter {
    fun formatMs(ms: Int) = formatMs(ms.toLong())
    fun formatMs(ms: Long): String {
        return formatMinSec(
            TimeUnit.MILLISECONDS.toMinutes(ms) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(ms) % TimeUnit.MINUTES.toSeconds(1)
        )
    }

    fun formatMinSec(m: Long, s: Long) = String.format("%02d:%02d", m, s)
}

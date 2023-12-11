package io.github.zyrouge.symphony.utils

import java.util.concurrent.TimeUnit

object DurationFormatter {
    fun formatMs(ms: Long) = formatMinSec(
        TimeUnit.MILLISECONDS.toDays(ms).floorDiv(TimeUnit.DAYS.toDays(1)),
        TimeUnit.MILLISECONDS.toHours(ms) % TimeUnit.DAYS.toHours(1),
        TimeUnit.MILLISECONDS.toMinutes(ms) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(ms) % TimeUnit.MINUTES.toSeconds(1)
    )

    fun formatMinSec(d: Long, h: Long, m: Long, s: Long) = when {
        d == 0L && h == 0L -> String.format("%02d:%02d", m, s)
        d == 0L -> String.format("%02d:%02d:%02d", h, m, s)
        else -> String.format("%02d:%02d:%02d:%02d", d, h, m, s)
    }
}

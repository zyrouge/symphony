package io.github.zyrouge.symphony.utils

import java.time.Duration

data class TimedContent(val pairs: List<Pair<Long, String>>) {
    val isSynced: Boolean get() = pairs.firstOrNull()?.first != pairs.lastOrNull()?.first

    companion object {
        val lrcLineSeparatorRegex = Regex("""\n|\r|\r\n""")
        val lrcLineFilterRegex = Regex("""^\[\s*(\d+):(\d+)\.(\d+)?\s*](.*)""")

        fun fromLyrics(content: String): TimedContent {
            var lastTime = 0L
            val pairs = content.split(lrcLineSeparatorRegex).map { x ->
                val match = lrcLineFilterRegex.matchEntire(x)
                val pair = when {
                    match != null -> Duration
                        .ofMinutes(match.groupValues[1].toLong())
                        .plusSeconds(match.groupValues[2].toLong())
                        .plusMillis(match.groupValues[3].toLong())
                        .toMillis() to match.groupValues[4].trim()

                    else -> lastTime to x.trim()
                }
                lastTime = pair.first
                pair
            }
            return TimedContent(pairs)
        }
    }
}

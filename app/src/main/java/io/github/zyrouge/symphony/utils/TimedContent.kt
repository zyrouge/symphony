package io.github.zyrouge.symphony.utils

import java.time.Duration

data class TimedContent(val pairs: List<Pair<Long, String>>) {
    companion object {
        val lrcLineRegex = Regex("""^\[\s*(\d+):(\d+)\.(\d+)?\s*](.*)""")

        fun fromLyrics(content: String): TimedContent {
            var lastTime = 0L
            val pairs = content.split("\n").mapNotNull { x ->
                if (x == "") return@mapNotNull null
                val match = lrcLineRegex.matchEntire(x)
                val pair = when {
                    match != null -> Duration
                        .ofMinutes(match.groupValues[1].toLong())
                        .plusSeconds(match.groupValues[2].toLong())
                        .plusMillis(match.groupValues[3].toLong())
                        .toMillis() to match.groupValues[4]

                    else -> lastTime to x
                }
                lastTime = pair.first
                pair
            }
            return TimedContent(pairs)
        }
    }
}
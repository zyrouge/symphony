package io.github.zyrouge.symphony.utils

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class TimedContent(val pairs: List<Pair<Long, String>>) {
    val isSynced: Boolean get() = pairs.firstOrNull()?.first != pairs.lastOrNull()?.first

    companion object {
        private val lrcLineFilterRegex = Regex("""^((?:\[\s*\d+:\d+\.\d+?\s*])+)(.*)$""")
        private val lrcTagLineRegex = Regex("""^\[([A-Za-z]+):(.*)]\s*$""")
        private val lrcTimeStampRegex = Regex("""\[\s*(\d+):(\d+\.\d+)?\s*]""")

        fun fromLyrics(content: String): TimedContent {
            var lastTime = 0L
            var offset = 0.milliseconds
            val pairs = content.lines().flatMap { line ->
                lrcTagLineRegex.matchEntire(line)?.let { tagMatch ->
                    val tag = tagMatch.groupValues[1].lowercase()
                    val value = tagMatch.groupValues[2]
                    if (tag == "offset") {
                        // positive offsets cause lyrics to appear sooner
                        offset = -value.toLong().milliseconds
                    }
                    listOf()
                } ?: lrcLineFilterRegex.matchEntire(line)?.let { lineMatch ->
                    val text = lineMatch.groupValues[2].trim()
                    lrcTimeStampRegex.findAll(lineMatch.groupValues[1]).map { timeMatch ->
                        lastTime = (timeMatch.groupValues[1].toLong().minutes
                                + timeMatch.groupValues[2].toDouble().seconds
                                + offset).inWholeMilliseconds
                        lastTime to text
                    }.toList()
                } ?: listOf(lastTime to line.trim())
            }.sortedBy { it.first }
            return TimedContent(pairs)
        }
    }
}
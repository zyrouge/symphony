package io.github.zyrouge.symphony

import io.github.zyrouge.symphony.utils.TimedContent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LyricsParserTest {
    @Test
    fun testUnsynced() = Assertions.assertEquals(
        TimedContent.fromLyrics("some line\nsome other line\n"),
        TimedContent(listOf(0L to "some line", 0L to "some other line", 0L to "")),
    )

    @Test
    fun testLineBreaks() = Assertions.assertEquals(
        TimedContent.fromLyrics("line1\nline2\rline3\r\nline4"),
        TimedContent((1..4).map { 0L to "line$it" }),
    )

    @Test
    fun testSynced() = Assertions.assertEquals(
        TimedContent.fromLyrics("[01:23.45]some line\n[12:34.56]some other line"),
        TimedContent(
            listOf(
                (1.minutes + 23.45.seconds).inWholeMilliseconds to "some line",
                (12.minutes + 34.56.seconds).inWholeMilliseconds to "some other line",
            )
        ),
    )

    @Test
    fun testSyncedEmptyLine() = Assertions.assertEquals(
        TimedContent.fromLyrics("[01:23.45]some line\n[02:34.56]\n[12:34.56]some other line"),
        TimedContent(
            listOf(
                (1.minutes + 23.45.seconds).inWholeMilliseconds to "some line",
                (2.minutes + 34.56.seconds).inWholeMilliseconds to "",
                (12.minutes + 34.56.seconds).inWholeMilliseconds to "some other line",
            )
        ),
    )

    @Test
    fun testMixedSyncedUnsynced() = Assertions.assertEquals(
        TimedContent.fromLyrics("[01:23.45]some line\nsome other line\n[12:34.56]a third line"),
        TimedContent(
            listOf(
                (1.minutes + 23.45.seconds).inWholeMilliseconds to "some line",
                (1.minutes + 23.45.seconds).inWholeMilliseconds to "some other line",
                (12.minutes + 34.56.seconds).inWholeMilliseconds to "a third line",
            )
        ),
    )

    @Test
    fun testRepeatedLines() = Assertions.assertEquals(
        TimedContent.fromLyrics("[01:23.45]some line\n[12:34.56][23:45.67]some other line"),
        TimedContent(
            listOf(
                (1.minutes + 23.45.seconds).inWholeMilliseconds to "some line",
                (12.minutes + 34.56.seconds).inWholeMilliseconds to "some other line",
                (23.minutes + 45.67.seconds).inWholeMilliseconds to "some other line",
            )
        ),
    )

    @Test
    fun testLinesSorted() = Assertions.assertEquals(
        TimedContent.fromLyrics("[00:01.23]first line\n[00:03.45]some line\n[00:2.34]some other line"),
        TimedContent(
            listOf(
                (1.23.seconds).inWholeMilliseconds to "first line",
                (2.34.seconds).inWholeMilliseconds to "some other line",
                (3.45.seconds).inWholeMilliseconds to "some line",
            )
        ),
    )

    @Test
    fun testRepeatedLinesSorted() = Assertions.assertEquals(
        TimedContent.fromLyrics("[00:01.23][00:03.45]some line\n[00:2.34]some other line"),
        TimedContent(
            listOf(
                (1.23.seconds).inWholeMilliseconds to "some line",
                (2.34.seconds).inWholeMilliseconds to "some other line",
                (3.45.seconds).inWholeMilliseconds to "some line",
            )
        ),
    )

    @Test
    fun testNonStandardTimeFormats() = Assertions.assertEquals(
        TimedContent.fromLyrics("[0:1.0]some line\n[23:45.6][123:45.6789]some other line"),
        TimedContent(
            listOf(
                (1.seconds).inWholeMilliseconds to "some line",
                (23.minutes + 45.6.seconds).inWholeMilliseconds to "some other line",
                // milliseconds are rounded down: `.6789` is parsed as `.678` and not `.679`
                (123.minutes + 45.678.seconds).inWholeMilliseconds to "some other line",
            )
        ),
    )

    @Test
    fun testOffset() = Assertions.assertEquals(
        TimedContent.fromLyrics("[offset:250]\n[12:34.56]some line\n[23:45.67]some other line"),
        TimedContent(
            listOf(
                (12.minutes + 34.56.seconds - 250.milliseconds).inWholeMilliseconds to "some line",
                (23.minutes + 45.67.seconds - 250.milliseconds).inWholeMilliseconds to "some other line",
            )
        ),
    )

    @Test
    fun testOffsetPlus() = Assertions.assertEquals(
        TimedContent.fromLyrics("[offset:+250]\n[12:34.56]some line\n[23:45.67]some other line"),
        TimedContent(
            listOf(
                (12.minutes + 34.56.seconds - 250.milliseconds).inWholeMilliseconds to "some line",
                (23.minutes + 45.67.seconds - 250.milliseconds).inWholeMilliseconds to "some other line",
            )
        ),
    )

    @Test
    fun testOffsetMinus() = Assertions.assertEquals(
        TimedContent.fromLyrics("[offset:-250]\n[12:34.56]some line\n[23:45.67]some other line"),
        TimedContent(
            listOf(
                (12.minutes + 34.56.seconds + 250.milliseconds).inWholeMilliseconds to "some line",
                (23.minutes + 45.67.seconds + 250.milliseconds).inWholeMilliseconds to "some other line",
            )
        ),
    )
}
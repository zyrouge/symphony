package io.github.zyrouge.symphony.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class DurationUtilsTest {
    @ParameterizedTest(name = "[{index}] milliseconds={0} expected={1}")
    @MethodSource("millisecondsCases")
    fun `formatMs renders elapsed duration with fixed width components`(
        milliseconds: Long,
        expected: String,
    ) {
        assertEquals(expected, DurationUtils.formatMs(milliseconds))
    }

    @ParameterizedTest(name = "[{index}] days={0} hours={1} minutes={2} seconds={3} expected={4}")
    @MethodSource("minutesAndSecondsCases")
    fun `formatMinSec renders duration components with fixed width`(
        days: Long,
        hours: Long,
        minutes: Long,
        seconds: Long,
        expected: String,
    ) {
        assertEquals(expected, DurationUtils.formatMinSec(days, hours, minutes, seconds))
    }

    companion object {
        @JvmStatic
        fun millisecondsCases(): Stream<Arguments> = Stream.of(
            Arguments.of(0L, "00:00"),
            Arguments.of(999L, "00:00"),
            Arguments.of(61_000L, "01:01"),
            Arguments.of(3_661_007L, "01:01:01"),
            Arguments.of(183_845_006L, "02:03:04:05"),
        )

        @JvmStatic
        fun minutesAndSecondsCases(): Stream<Arguments> = Stream.of(
            Arguments.of(0L, 0L, 2L, 3L, "02:03"),
            Arguments.of(0L, 1L, 2L, 3L, "01:02:03"),
            Arguments.of(1L, 2L, 3L, 4L, "01:02:03:04"),
        )
    }
}

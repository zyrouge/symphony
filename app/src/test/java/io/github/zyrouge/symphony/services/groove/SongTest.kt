package io.github.zyrouge.symphony.services.groove

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class SongTest {
    @ParameterizedTest(name = "[{index}] value={0} expected={2}")
    @MethodSource("singleValueCases")
    fun `parseMultiValue splits a single tag value`(
        value: String?,
        regex: Regex,
        expected: Set<String>,
    ) {
        assertEquals(expected, Song.parseMultiValue(value, regex))
    }

    @ParameterizedTest(name = "[{index}] values={0} expected={2}")
    @MethodSource("multipleValueCases")
    fun `parseMultiValue merges multiple tag values`(
        values: Set<String>,
        regex: Regex,
        expected: Set<String>,
    ) {
        assertEquals(expected, Song.parseMultiValue(values, regex))
    }

    companion object {
        private val comma = Regex(",")
        private val semicolon = Regex(";")
        private val doubleColonOrComma = Regex("::|,")

        @JvmStatic
        fun singleValueCases(): Stream<Arguments> = Stream.of(
            Arguments.of(null as String?, comma, emptySet<String>()),
            Arguments.of("", comma, emptySet<String>()),
            Arguments.of(" \t ", comma, emptySet<String>()),
            Arguments.of(" A, ,B,,A ", comma, setOf("A", "B")),
            Arguments.of("A::B,C", doubleColonOrComma, setOf("A", "B", "C")),
            Arguments.of("A,,B", comma, setOf("A", "B")),
        )

        @JvmStatic
        fun multipleValueCases(): Stream<Arguments> = Stream.of(
            Arguments.of(setOf("A;B", "B;C"), semicolon, setOf("A", "B", "C")),
        )
    }
}

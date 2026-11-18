package io.github.zyrouge.symphony.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class SimplePathTest {
    @ParameterizedTest(name = "[{index}] path={0} expectedParts={1}")
    @MethodSource("normalizedPartsCases")
    fun `constructor normalizes separators and relative segments`(
        path: String,
        expectedParts: List<String>,
    ) {
        assertEquals(expectedParts, SimplePath(path).parts)
    }

    @ParameterizedTest(name = "[{index}] path={0} expectedPathString={1}")
    @MethodSource("pathStringCases")
    fun `pathString joins the normalized parts with a slash`(
        path: String,
        expectedPathString: String,
    ) {
        assertEquals(expectedPathString, SimplePath(path).pathString)
    }

    @ParameterizedTest(name = "[{index}] path={0} expectedParentParts={1}")
    @MethodSource("parentPartsCases")
    fun `parent exposes the parts above the last segment`(
        path: String,
        expectedParentParts: List<String>,
    ) {
        assertEquals(expectedParentParts, SimplePath(path).parent?.parts)
    }

    @ParameterizedTest(name = "[{index}] path={0} expectedNameWithoutExtension={1}")
    @MethodSource("nameWithoutExtensionCases")
    fun `nameWithoutExtension keeps every dot before the last one`(
        path: String,
        expectedNameWithoutExtension: String,
    ) {
        assertEquals(expectedNameWithoutExtension, SimplePath(path).nameWithoutExtension)
    }

    @ParameterizedTest(name = "[{index}] path={0} expectedExtension={1}")
    @MethodSource("extensionCases")
    fun `extension is the text after the last dot`(
        path: String,
        expectedExtension: String,
    ) {
        assertEquals(expectedExtension, SimplePath(path).extension)
    }

    @ParameterizedTest(name = "[{index}] path={0} expectedSize={1}")
    @MethodSource("sizeCases")
    fun `size counts the normalized parts`(
        path: String,
        expectedSize: Int,
    ) {
        assertEquals(expectedSize, SimplePath(path).size)
    }

    @Test
    fun `parent chain walks up one segment at a time and ends at null`() {
        val path = SimplePath("a/b/c")
        val firstParent = path.parent
        val secondParent = firstParent?.parent
        val thirdParent = secondParent?.parent

        assertEquals(listOf("a", "b"), firstParent?.parts)
        assertEquals(listOf("a"), secondParent?.parts)
        assertNull(thirdParent)
    }

    @Test
    fun `join resolves relative segments against the existing parts`() {
        val joined = SimplePath("Music//Rock").join(".", "Live", "..", "Set.01.flac")

        assertEquals(listOf("Music", "Rock", "Set.01.flac"), joined.parts)
        assertEquals("Set.01", joined.nameWithoutExtension)
        assertEquals("flac", joined.extension)
        assertEquals(3, joined.size)
    }

    companion object {
        @JvmStatic
        fun normalizedPartsCases(): Stream<Arguments> = Stream.of(
            Arguments.of("Music\\Rock/./Live/../Song.flac", listOf("Music", "Rock", "Song.flac")),
            Arguments.of("Music//Rock", listOf("Music", "Rock")),
            Arguments.of("a/b/c", listOf("a", "b", "c")),
        )

        @JvmStatic
        fun pathStringCases(): Stream<Arguments> = Stream.of(
            Arguments.of("Music\\Rock/./Live/../Song.flac", "Music/Rock/Song.flac"),
            Arguments.of("Music//Rock", "Music/Rock"),
            Arguments.of("a/b/c", "a/b/c"),
        )

        @JvmStatic
        fun parentPartsCases(): Stream<Arguments> = Stream.of(
            Arguments.of("Music\\Rock/./Live/../Song.flac", listOf("Music", "Rock")),
            Arguments.of("a/b/c", listOf("a", "b")),
        )

        @JvmStatic
        fun nameWithoutExtensionCases(): Stream<Arguments> = Stream.of(
            Arguments.of("folder/README", "README"),
            Arguments.of("Set.01.flac", "Set.01"),
        )

        @JvmStatic
        fun extensionCases(): Stream<Arguments> = Stream.of(
            Arguments.of("folder/README", ""),
            Arguments.of("Set.01.flac", "flac"),
        )

        @JvmStatic
        fun sizeCases(): Stream<Arguments> = Stream.of(
            Arguments.of("Music//Rock", 2),
            Arguments.of("Set.01.flac", 1),
            Arguments.of("a/b/c", 3),
        )
    }
}

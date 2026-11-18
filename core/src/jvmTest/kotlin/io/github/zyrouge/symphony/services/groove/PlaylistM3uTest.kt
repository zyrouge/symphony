package io.github.zyrouge.symphony.services.groove

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaylistM3uTest {
    @Test
    fun `parseM3u keeps song lines in order`() {
        val content = "Music/a.flac\nMusic/b.mp3\n"

        assertEquals(listOf("Music/a.flac", "Music/b.mp3"), parseM3u(content))
    }

    @Test
    fun `parseM3u drops comments and blank lines`() {
        val content = """
            #EXTM3U
            Music/a.flac

              #EXTINF:123,Artist - Title
            Music/b.mp3
        """.trimIndent()

        assertEquals(listOf("Music/a.flac", "Music/b.mp3"), parseM3u(content))
    }

    @Test
    fun `parseM3u trims surrounding whitespace`() {
        assertEquals(listOf("Music/a.flac"), parseM3u("  Music/a.flac  \n"))
    }

    @Test
    fun `parseM3u returns nothing for empty content`() {
        assertEquals(emptyList<String>(), parseM3u(""))
    }

    @Test
    fun `buildM3u joins song paths with newlines`() {
        assertEquals("Music/a.flac\nMusic/b.mp3", buildM3u(listOf("Music/a.flac", "Music/b.mp3")))
    }

    @Test
    fun `buildM3u round-trips through parseM3u`() {
        val paths = listOf("Music/a.flac", "Music/b.mp3")

        assertEquals(paths, parseM3u(buildM3u(paths)))
    }
}

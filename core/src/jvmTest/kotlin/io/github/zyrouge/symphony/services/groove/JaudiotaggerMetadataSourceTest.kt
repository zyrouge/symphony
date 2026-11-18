package io.github.zyrouge.symphony.services.groove

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class JaudiotaggerMetadataSourceTest {
    @TempDir
    lateinit var temp: Path

    private fun stage(name: String): PathAudioFile {
        val target = temp.resolve(name)
        val stream = javaClass.classLoader.getResourceAsStream(name)
        assertNotNull(stream, "fixture $name missing from the test classpath")
        stream!!.use { input ->
            Files.newOutputStream(target).use { output -> input.copyTo(output) }
        }
        return PathAudioFile(target)
    }

    private fun fixtures() = listOf(
        Fixture("audio.flac", hasGenres = true),
        Fixture("audio.mp3", hasGenres = false),
        Fixture("audio-id3v2.3.mp3", hasGenres = false),
        Fixture("audio-id3v2.4.mp3", hasGenres = false),
        Fixture("audio.ogg", hasGenres = true),
        Fixture("audio.m4a", hasGenres = false),
    )

    @Test
    fun `reads every fixture`() {
        for (fixture in fixtures()) {
            val metadata = JaudiotaggerMetadataSource.read(stage(fixture.name))

            assertNotNull(metadata, "jaudiotagger returned nothing for ${fixture.name}")
            assertTrue(
                (metadata!!.durationRaw ?: 0L) > 0L,
                "jaudiotagger produced no duration for ${fixture.name}",
            )
        }
    }

    @Test
    fun `reads the tagged fields every fixture carries`() {
        for (fixture in fixtures()) {
            val metadata = JaudiotaggerMetadataSource.read(stage(fixture.name))!!

            assertEquals("Demo Audio", metadata.title, "title of ${fixture.name}")
            assertEquals("Demo Album", metadata.album, "album of ${fixture.name}")
            assertTrue(
                metadata.artists.any { it.contains("Demo Artist 1") },
                "artists of ${fixture.name} must keep the first value",
            )
            assertTrue(
                metadata.artists.any { it.contains("Demo Artist 2") },
                "artists of ${fixture.name} must keep the second value",
            )
            assertNotNull(metadata.picture, "picture of ${fixture.name} must be read")
            assertEquals("image/png", metadata.picture!!.mimeType)
        }
    }

    @Test
    fun `reads genres only where they exist`() {
        for (fixture in fixtures().filter { it.hasGenres }) {
            val metadata = JaudiotaggerMetadataSource.read(stage(fixture.name))!!

            assertEquals(setOf("Rap", "Rock"), metadata.genres, "genres of ${fixture.name}")
        }
    }

    @Test
    fun `reports positive numbers for the technical fields`() {
        for (fixture in fixtures()) {
            val metadata = JaudiotaggerMetadataSource.read(stage(fixture.name))!!

            assertTrue(
                (metadata.samplingRate ?: 0L) > 0L,
                "sampling rate of ${fixture.name} must be positive",
            )
            metadata.bitrateRaw?.let {
                assertTrue(it > 0L, "bitrate of ${fixture.name} must be positive")
            }
        }
    }

    @Test
    fun `declares the metaphony-compatible policies`() {
        assertEquals("", JaudiotaggerMetadataSource.idSuffix)
        assertTrue(JaudiotaggerMetadataSource.durationIsSeconds)
        assertTrue(JaudiotaggerMetadataSource.bitrateIsKilobits)
        assertTrue(JaudiotaggerMetadataSource.storesLyrics)
        assertTrue(JaudiotaggerMetadataSource.failuresFallBack)
        assertTrue(JaudiotaggerMetadataSource.artworkPolicy.nameWithMimeExtension)
        assertTrue(JaudiotaggerMetadataSource.artworkPolicy.dropWhenMimeUnknown)
        assertTrue(JaudiotaggerMetadataSource.artworkPolicy.copyBytesWhenNoResize)
    }

    @Test
    fun `resolves into the same song fields the android primary reader produces`() {
        val file = stage("audio.flac")
        val raw = JaudiotaggerMetadataSource.read(file)!!
        val resolution = buildSongFields(
            fileInfo = FileInfo(
                name = file.name,
                lastModified = file.lastModified,
                size = file.size,
                pathName = file.pathName,
            ),
            options = ResolutionOptions(
                artistSeparatorRegex = Regex(";"),
                genreSeparatorRegex = Regex(","),
            ),
            id = "7",
            source = JaudiotaggerMetadataSource,
            raw = raw,
            artworkQualityMaxSide = null,
        )

        assertEquals("7", resolution.fields.id)
        assertEquals(raw.durationRaw!! * 1000L, resolution.fields.duration)
        assertEquals(CoverMode.RAW_COPY, resolution.coverPlan.mode)
        assertEquals("7.png", resolution.coverPlan.fileName)
        assertEquals(resolution.coverPlan.fileName, resolution.fields.coverFile)
        assertEquals("Demo Audio", resolution.fields.title)
        assertNull(resolution.lyricsToStore)
    }

    private data class Fixture(
        val name: String,
        val hasGenres: Boolean,
    )
}

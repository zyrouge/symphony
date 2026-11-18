package io.github.zyrouge.symphony.services.groove

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.zyrouge.symphony.utils.DocumentFileX
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioMetadataSourcesInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun stage(assetName: String): DocumentFileX {
        val target = File(context.cacheDir, assetName)
        context.assets.open(assetName).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return DocumentFileX(
            context = context,
            id = assetName,
            name = assetName,
            mimeType = "audio/*",
            lastModified = target.lastModified(),
            size = target.length(),
            uri = Uri.fromFile(target),
        )
    }

    /**
     * Expectations decoded from the fixture bytes with a structural parser (Vorbis comments, ID3 frames,
     * MP4 atoms), not guessed: all six files carry `Demo Audio`, `Demo Album`, the artist
     * `Demo Artist 1; Demo Artist 2` (ID3v2.4 separates with NUL) and an embedded PNG; only the flac and
     * the ogg carry `GENRE=Rap` + `GENRE=Rock`.
     */
    private fun fixtures() = listOf(
        Fixture("audio.flac", hasGenres = true),
        Fixture("audio.mp3", hasGenres = false),
        Fixture("audio-id3v2.3.mp3", hasGenres = false),
        Fixture("audio-id3v2.4.mp3", hasGenres = false),
        Fixture("audio.ogg", hasGenres = true),
        Fixture("audio.m4a", hasGenres = false),
    )

    @Test
    fun metaphonySourceReadsEveryFixture() {
        for (fixture in fixtures()) {
            val metadata = MetaphonyMetadataSource.read(context, stage(fixture.name))

            assertNotNull("metaphony returned nothing for ${fixture.name}", metadata)
            assertNotNull("metaphony produced no duration for ${fixture.name}", metadata!!.durationRaw)
            assertTrue("metaphony duration must be positive for ${fixture.name}", metadata.durationRaw!! > 0)
        }
    }

    @Test
    fun metaphonySourceReadsTaggedFields() {
        for (fixture in fixtures()) {
            val metadata = MetaphonyMetadataSource.read(context, stage(fixture.name))!!

            assertEquals("title of ${fixture.name}", "Demo Audio", metadata.title)
            assertEquals("album of ${fixture.name}", "Demo Album", metadata.album)
            assertTrue(
                "artists of ${fixture.name} must keep the first value",
                metadata.artists.any { it.contains("Demo Artist 1") },
            )
            assertTrue(
                "artists of ${fixture.name} must keep the second value",
                metadata.artists.any { it.contains("Demo Artist 2") },
            )
            assertNotNull("picture of ${fixture.name} must be read", metadata.picture)
        }
    }

    @Test
    fun metaphonySourceReadsGenresOnlyWhereTheyExist() {
        for (fixture in fixtures().filter { it.hasGenres }) {
            val metadata = MetaphonyMetadataSource.read(context, stage(fixture.name))!!

            assertEquals("genres of ${fixture.name}", setOf("Rap", "Rock"), metadata.genres)
        }
    }

    @Test
    fun metaphonyBitrateIsInKilobitsAndScalesToTheRetrieverBits() {
        var sawMetaphonyBitrate = false
        for (fixture in fixtures()) {
            val file = stage(fixture.name)
            val metaphony = MetaphonyMetadataSource.read(context, file)!!
            val retriever = MediaMetadataRetrieverSource.read(context, file)!!
            val kilobits = metaphony.bitrateRaw
            if (kilobits != null) {
                sawMetaphonyBitrate = true
                val bits = retriever.bitrateRaw
                if (bits != null) {
                    val scaled = kilobits * 1000
                    assertTrue(
                        "bitrate of ${fixture.name}: $kilobits kbps should be ~$bits bps",
                        scaled in (bits * 3 / 4)..(bits * 5 / 4),
                    )
                }
            }
        }
        assertTrue("no fixture reported a metaphony bitrate at all", sawMetaphonyBitrate)
    }

    @Test
    fun retrieverSourceReadsEveryFixture() {
        for (fixture in fixtures()) {
            val metadata = MediaMetadataRetrieverSource.read(context, stage(fixture.name))

            assertNotNull("retriever returned nothing for ${fixture.name}", metadata)
            assertNotNull("retriever produced no duration for ${fixture.name}", metadata!!.durationRaw)
            assertTrue(
                "retriever duration must be positive for ${fixture.name}",
                metadata.durationRaw!! > 0,
            )
            assertNull("retriever must not invent channels for ${fixture.name}", metadata.channels)
            assertNull("retriever must not invent an encoder for ${fixture.name}", metadata.encoder)
        }
    }

    @Test
    fun theTwoSourcesReportDurationInDifferentUnits() {
        for (fixture in fixtures()) {
            val file = stage(fixture.name)
            val seconds = MetaphonyMetadataSource.read(context, file)!!.durationRaw!!
            val milliseconds = MediaMetadataRetrieverSource.read(context, file)!!.durationRaw!!

            assertTrue(
                "expected ${fixture.name} to differ by roughly 1000x, got $seconds s vs $milliseconds ms",
                milliseconds in (seconds * 1000 - 2000)..(seconds * 1000 + 2000),
            )
        }
    }

    @Test
    fun sourcesDeclareTheirDocumentedPolicies() {
        assertEquals("", MetaphonyMetadataSource.idSuffix)
        assertEquals(".mr", MediaMetadataRetrieverSource.idSuffix)
        assertTrue(MetaphonyMetadataSource.durationIsSeconds)
        assertTrue(MetaphonyMetadataSource.bitrateIsKilobits)
        assertTrue(MetaphonyMetadataSource.failuresFallBack)
        assertEquals(false, MediaMetadataRetrieverSource.durationIsSeconds)
        assertEquals(false, MediaMetadataRetrieverSource.bitrateIsKilobits)
        assertEquals(false, MediaMetadataRetrieverSource.failuresFallBack)
    }

    private data class Fixture(
        val name: String,
        val hasGenres: Boolean,
    )
}

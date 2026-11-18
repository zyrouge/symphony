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
     * Expectations read directly from the fixture bytes, not guessed: the flac/ogg Vorbis comments and
     * the three ID3 files carry `TITLE=Demo Audio`, `ALBUM=Demo Album`, the multi-value artist
     * `Demo Artist 1; Demo Artist 2`, and an embedded png (ogg and m4a carry no picture); `audio.m4a`
     * has no text tags at all.
     */
    private fun fixtures() = listOf(
        Fixture("audio.flac", hasTags = true, hasPicture = true),
        Fixture("audio.mp3", hasTags = true, hasPicture = true),
        Fixture("audio-id3v2.3.mp3", hasTags = true, hasPicture = true),
        Fixture("audio-id3v2.4.mp3", hasTags = true, hasPicture = true),
        Fixture("audio.ogg", hasTags = true, hasPicture = false),
        Fixture("audio.m4a", hasTags = false, hasPicture = false),
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
        for (fixture in fixtures().filter { it.hasPicture || it.hasTags }) {
            val metadata = MetaphonyMetadataSource.read(context, stage(fixture.name))!!

            assertEquals("title of ${fixture.name}", "Demo Audio", metadata.title)
            assertEquals("album of ${fixture.name}", "Demo Album", metadata.album)
            assertTrue(
                "artists of ${fixture.name} must keep both values",
                metadata.artists.any { it.contains("Demo Artist 1") },
            )
            assertTrue(
                "artists of ${fixture.name} must keep the second value",
                metadata.artists.any { it.contains("Demo Artist 2") },
            )
            assertEquals(
                "picture presence of ${fixture.name}",
                fixture.hasPicture,
                metadata.picture != null,
            )
        }
    }

    @Test
    fun metaphonySourceReportsEnglishGenres() {
        val flac = MetaphonyMetadataSource.read(context, stage("audio.flac"))!!

        assertEquals(setOf("Rap", "Rock"), flac.genres)
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
        val hasTags: Boolean,
        val hasPicture: Boolean,
    )
}

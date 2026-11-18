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

    @Test
    fun metaphonySourceReadsRealFiles() {
        for (asset in AUDIO_FIXTURES) {
            val metadata = MetaphonyMetadataSource.read(context, stage(asset))

            assertNotNull("metaphony returned nothing for $asset", metadata)
            assertNotNull("metaphony produced no duration for $asset", metadata!!.durationRaw)
            assertTrue("metaphony duration must be positive for $asset", metadata.durationRaw!! > 0)
            assertTrue("metaphony must report a title for $asset", metadata.title != null)
        }
    }

    @Test
    fun retrieverSourceReadsRealFiles() {
        for (asset in AUDIO_FIXTURES) {
            val metadata = MediaMetadataRetrieverSource.read(context, stage(asset))

            assertNotNull("retriever returned nothing for $asset", metadata)
            assertNotNull("retriever produced no duration for $asset", metadata!!.durationRaw)
            assertTrue("retriever duration must be positive for $asset", metadata.durationRaw!! > 0)
            assertNull("retriever must not invent channels for $asset", metadata.channels)
            assertNull("retriever must not invent an encoder for $asset", metadata.encoder)
        }
    }

    @Test
    fun theTwoSourcesReportDurationInDifferentUnits() {
        for (asset in AUDIO_FIXTURES) {
            val file = stage(asset)
            val seconds = MetaphonyMetadataSource.read(context, file)!!.durationRaw!!
            val milliseconds = MediaMetadataRetrieverSource.read(context, file)!!.durationRaw!!

            assertTrue(
                "expected $asset to differ by roughly 1000x, got $seconds s vs $milliseconds ms",
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

    private companion object {
        val AUDIO_FIXTURES = listOf("audio.flac", "audio.mp3", "audio.ogg")
    }
}

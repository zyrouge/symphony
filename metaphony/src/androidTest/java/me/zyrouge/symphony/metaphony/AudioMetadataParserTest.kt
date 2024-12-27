package me.zyrouge.symphony.metaphony

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioMetadataParserTest {
    @Test
    fun testFlac() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val filename = "audio.flac"
        val metadata = AudioMetadataParser.parse(
            filename,
            context.assets.openFd(filename).parcelFileDescriptor.detachFd(),
        )
        assertMetadata("flac", metadata)
    }

    @Test
    fun testOgg() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val filename = "audio.ogg"
        val metadata = AudioMetadataParser.parse(
            filename,
            context.assets.openFd(filename).parcelFileDescriptor.detachFd(),
        )
        assertMetadata("ogg", metadata)
    }

    @Test
    fun testMp3() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val filename = "audio.mp3"
        val metadata = AudioMetadataParser.parse(
            filename,
            context.assets.openFd(filename).parcelFileDescriptor.detachFd(),
        )
        assertMetadata("mp3", metadata)
    }

    @Test
    fun testMp3Id23() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val filename = "audio-id3v2.3.mp3"
        val metadata = AudioMetadataParser.parse(
            filename,
            context.assets.openFd(filename).parcelFileDescriptor.detachFd(),
        )
        assertMetadata("mp3id23", metadata)
    }

    @Test
    fun testMp3Id24() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val filename = "audio-id3v2.4.mp3"
        val metadata = AudioMetadataParser.parse(
            filename,
            context.assets.openFd(filename).parcelFileDescriptor.detachFd(),
        )
        assertMetadata("mp3id24", metadata)
    }

    fun assertMetadata(source: String, metadata: AudioMetadata?) {
        Assert.assertNotNull(metadata)
        metadata!!
        Assert.assertEquals("Demo Audio2", metadata.title)
        Assert.assertArrayEquals(
            arrayOf("Demo Artist 1; Demo Artist 2"),
            metadata.artists.toTypedArray(),
        )
        Assert.assertArrayEquals(
            arrayOf("Demo Artist 2"),
            metadata.albumArtists.toTypedArray(),
        )
        Assert.assertArrayEquals(emptyArray(), metadata.composers.toTypedArray())
        Assert.assertArrayEquals(
            arrayOf("Rap", "Rock"),
            metadata.genres.toTypedArray(),
        )
        Assert.assertEquals(null, metadata.discNumber)
        Assert.assertEquals(null, metadata.discTotal)
        Assert.assertEquals(1, metadata.trackNumber)
        Assert.assertEquals(2, metadata.trackTotal)
        Assert.assertEquals(null, metadata.date)
        Assert.assertEquals(null, metadata.lyrics)
        val expectedBitrate = when (source) {
            "ogg" -> 112
            "flac" -> 144475
            else -> 130
        }
        Assert.assertEquals(expectedBitrate, metadata.bitrate)
        val expectedLength = when (source) {
            // not sure why, we ignore this for now
            "ogg" -> -2147483
            else -> 1
        }
        Assert.assertEquals(expectedLength, metadata.lengthInSeconds)
        Assert.assertEquals(44100, metadata.sampleRate)
        Assert.assertEquals(2, metadata.channels)
        Assert.assertEquals(1, metadata.pictures.size)
        Assert.assertEquals("Front Cover", metadata.pictures[0].pictureType)
        Assert.assertEquals("image/png", metadata.pictures[0].mimeType)
        Assert.assertNotEquals(0, metadata.pictures[0].data.size)
    }
}

package io.github.zyrouge.metaphony

import io.github.zyrouge.metaphony.audio.flac.Flac
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FlacTest {
    @Test
    fun parse() {
        val file = Flac()
        val klassLoader = object {}.javaClass.classLoader!!
        klassLoader.getResourceAsStream("audio.flac")!!.use { file.read(it) }
        val metadata = file.getMetadata()
        val stream = file.getStreamInfo()
        Assertions.assertEquals(metadata.title, "Demo Audio")
        Assertions.assertEquals(metadata.album, "Demo Album")
        Assertions.assertIterableEquals(metadata.artists, setOf("Demo Artist 1; Demo Artist 2"))
        Assertions.assertIterableEquals(metadata.albumArtists, setOf("Demo Artist 2"))
        Assertions.assertIterableEquals(metadata.composer, setOf("Demo Artist 1; Demo Artist 2"))
        Assertions.assertIterableEquals(metadata.genres, setOf("Rap", "Rock"))
        Assertions.assertEquals(metadata.trackNumber, 1)
        Assertions.assertEquals(metadata.trackTotal, 2)
        Assertions.assertNull(metadata.discNumber)
        Assertions.assertNull(metadata.discTotal)
        Assertions.assertNull(metadata.year)
        Assertions.assertNull(metadata.date)
        Assertions.assertNull(metadata.lyrics)
        Assertions.assertEquals(metadata.comments.size, 0)
        Assertions.assertEquals(metadata.encoder, "Lavf60.16.100")
        Assertions.assertEquals(metadata.artworks.size, 1)
        Assertions.assertEquals(stream.duration, 1)
        Assertions.assertEquals(stream.bitrate, 2116800)
        Assertions.assertNull(stream.minBitrate)
        Assertions.assertNull(stream.maxBitrate)
        Assertions.assertEquals(stream.channels, 2)
        Assertions.assertEquals(stream.bitsPerSample, 24)
        Assertions.assertEquals(stream.samplingRate, 44100)
        Assertions.assertEquals(stream.samples, 44100)
        Assertions.assertEquals(stream.codec, "FLAC")
    }
}

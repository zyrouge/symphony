package io.github.zyrouge.metaphony

import io.github.zyrouge.metaphony.audio.mp3.Mp3
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Mp3Test {
    @Test
    fun parse() {
        val file = Mp3()
        val klassLoader = object {}.javaClass.classLoader!!
        klassLoader.getResourceAsStream("audio.mp3")!!.use { file.read(it) }
        val metadata = file.getMetadata()
        val stream = file.getStreamInfo()
        Assertions.assertEquals(metadata.title, "Demo Audio")
        Assertions.assertEquals(metadata.album, "Demo Album")
        Assertions.assertIterableEquals(metadata.artists, setOf("Demo Artist 1; Demo Artist 2"))
        Assertions.assertIterableEquals(metadata.albumArtists, setOf("Demo Artist 2"))
        // TODO: fix composers
        Assertions.assertEquals(metadata.composer.size, 0)
        Assertions.assertIterableEquals(metadata.genres, setOf("Rap", "Rock"))
        Assertions.assertEquals(metadata.trackNumber, 1)
        Assertions.assertEquals(metadata.trackTotal, 2)
        Assertions.assertNull(metadata.discNumber)
        Assertions.assertNull(metadata.discTotal)
        Assertions.assertNull(metadata.year)
        Assertions.assertNull(metadata.date)
        Assertions.assertNull(metadata.lyrics)
        Assertions.assertEquals(metadata.comments.size, 0)
        // TODO: fix encoder
        Assertions.assertEquals(metadata.encoder, null)
        Assertions.assertEquals(metadata.artworks.size, 1)
        Assertions.assertEquals(stream.duration, 1)
        Assertions.assertEquals(stream.bitrate, 64000)
        Assertions.assertNull(stream.minBitrate)
        Assertions.assertNull(stream.maxBitrate)
        Assertions.assertEquals(stream.channels, 2)
        Assertions.assertNull(stream.bitsPerSample)
        Assertions.assertEquals(stream.samplingRate, 44100)
        Assertions.assertEquals(stream.samples, 1152)
        Assertions.assertEquals(stream.codec, "MPEG-1")
    }
}

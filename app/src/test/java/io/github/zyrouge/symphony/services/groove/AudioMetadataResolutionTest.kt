package io.github.zyrouge.symphony.services.groove

import android.content.Context
import io.github.zyrouge.symphony.utils.DocumentFileX
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AudioMetadataResolutionTest {
    private val artistRegex = Regex(";")
    private val genreRegex = Regex(",")

    private val fileInfo = FileInfo(
        name = "Track.flac",
        lastModified = 1_700_000_000_000L,
        size = 12_345L,
        pathName = "Music/Rock/Track.flac",
    )

    private val options = ResolutionOptions(
        artistSeparatorRegex = artistRegex,
        genreSeparatorRegex = genreRegex,
    )

    private fun rawMetadata(
        title: String? = null,
        album: String? = null,
        artists: Set<String> = emptySet(),
        albumArtists: Set<String> = emptySet(),
        composers: Set<String> = emptySet(),
        genres: Set<String> = emptySet(),
        trackNumber: Int? = null,
        trackTotal: Int? = null,
        discNumber: Int? = null,
        discTotal: Int? = null,
        date: LocalDate? = null,
        year: Int? = null,
        durationRaw: Long? = null,
        bitrateRaw: Long? = null,
        samplingRate: Long? = null,
        channels: Int? = null,
        encoder: String? = null,
        lyrics: String? = null,
        picture: EmbeddedPicture? = null,
    ) = RawAudioMetadata(
        title = title,
        album = album,
        artists = artists,
        albumArtists = albumArtists,
        composers = composers,
        genres = genres,
        trackNumber = trackNumber,
        trackTotal = trackTotal,
        discNumber = discNumber,
        discTotal = discTotal,
        date = date,
        year = year,
        durationRaw = durationRaw,
        bitrateRaw = bitrateRaw,
        samplingRate = samplingRate,
        channels = channels,
        encoder = encoder,
        lyrics = lyrics,
        picture = picture,
    )

    private class FakeSource(
        override val idSuffix: String = "",
        override val artworkPolicy: ArtworkPolicy = ArtworkPolicy(true, true, true),
        override val storesLyrics: Boolean = false,
        override val durationIsSeconds: Boolean = true,
        override val bitrateIsKilobits: Boolean = true,
        override val failuresFallBack: Boolean = true,
    ) : AudioMetadataSource {
        override fun read(context: Context, file: DocumentFileX): RawAudioMetadata? =
            throw UnsupportedOperationException("the reader lambda is used instead")
    }

    private val metaphonyLike = FakeSource(
        idSuffix = "",
        storesLyrics = true,
        durationIsSeconds = true,
        bitrateIsKilobits = true,
        failuresFallBack = true,
    )

    private val retrieverLike = FakeSource(
        idSuffix = ".mr",
        artworkPolicy = ArtworkPolicy(false, false, false),
        storesLyrics = false,
        durationIsSeconds = false,
        bitrateIsKilobits = false,
        failuresFallBack = false,
    )

    @Test
    fun `readFirstAvailable takes the first source that yields metadata`() {
        val first = rawMetadata(title = "First")
        val second = rawMetadata(title = "Second")

        val result = readFirstAvailable(listOf(metaphonyLike, retrieverLike)) { source ->
            if (source === metaphonyLike) first else second
        }

        assertNotNull(result)
        assertEquals("First", result!!.title)
    }

    @Test
    fun `readFirstAvailable falls through when a source yields nothing`() {
        val second = rawMetadata(title = "Second")

        val result = readFirstAvailable(listOf(metaphonyLike, retrieverLike)) { source ->
            if (source === metaphonyLike) null else second
        }

        assertEquals("Second", result!!.title)
    }

    @Test
    fun `readFirstAvailable reports a fallible failure once then uses the next source`() {
        val errors = mutableListOf<Pair<AudioMetadataSource, Exception>>()
        val second = rawMetadata(title = "Second")

        val result = readFirstAvailable<RawAudioMetadata>(
            sources = listOf(metaphonyLike, retrieverLike),
            onSourceError = { source, err -> errors.add(source to err) },
            attempt = { source ->
                if (source === metaphonyLike) {
                    throw IllegalStateException("metaphony blew up")
                } else {
                    second
                }
            },
        )

        assertEquals("Second", result!!.title)
        assertEquals(1, errors.size)
        assertEquals(metaphonyLike, errors.single().first)
        assertEquals("metaphony blew up", errors.single().second.message)
    }

    @Test
    fun `readFirstAvailable propagates a failure from a non-fallible source`() {
        val error = assertThrows<IllegalStateException> {
            readFirstAvailable(listOf(retrieverLike)) { throw IllegalStateException("retriever") }
        }

        assertEquals("retriever", error.message)
    }

    @Test
    fun `readFirstAvailable returns null when every source yields nothing`() {
        assertNull(readFirstAvailable(listOf(metaphonyLike, retrieverLike)) { null })
    }

    @Test
    fun `buildSongFields appends the source id suffix`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "42", source = retrieverLike,
            raw = rawMetadata(title = "T"), artworkQualityMaxSide = null,
        )

        assertEquals("42.mr", resolution.fields.id)
    }

    @Test
    fun `buildSongFields leaves the id untouched for the suffix-free source`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "42", source = metaphonyLike,
            raw = rawMetadata(title = "T"), artworkQualityMaxSide = null,
        )

        assertEquals("42", resolution.fields.id)
    }

    @Test
    fun `buildSongFields converts metaphony seconds into milliseconds`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(durationRaw = 183L), artworkQualityMaxSide = null,
        )

        assertEquals(183_000L, resolution.fields.duration)
    }

    @Test
    fun `buildSongFields passes retriever milliseconds through unchanged`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(durationRaw = 183_000L), artworkQualityMaxSide = null,
        )

        assertEquals(183_000L, resolution.fields.duration)
    }

    @Test
    fun `buildSongFields folds a missing duration to zero on both paths`() {
        val metaphony = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(durationRaw = null), artworkQualityMaxSide = null,
        )
        val retriever = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(durationRaw = null), artworkQualityMaxSide = null,
        )

        assertEquals(0L, metaphony.fields.duration)
        assertEquals(0L, retriever.fields.duration)
    }

    @Test
    fun `buildSongFields converts metaphony kilobits into bits`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(bitrateRaw = 320L), artworkQualityMaxSide = null,
        )

        assertEquals(320_000L, resolution.fields.bitrate)
    }

    @Test
    fun `buildSongFields passes retriever bits through unchanged`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(bitrateRaw = 320_000L), artworkQualityMaxSide = null,
        )

        assertEquals(320_000L, resolution.fields.bitrate)
    }

    @Test
    fun `buildSongFields keeps a missing bitrate missing`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(bitrateRaw = null), artworkQualityMaxSide = null,
        )

        assertNull(resolution.fields.bitrate)
    }

    @Test
    fun `buildSongFields preserves the null capabilities carried by the raw metadata`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(discNumber = 2, discTotal = null, channels = null, encoder = null),
            artworkQualityMaxSide = null,
        )

        assertEquals(2, resolution.fields.discNumber)
        assertNull(resolution.fields.discTotal)
        assertNull(resolution.fields.channels)
        assertNull(resolution.fields.encoder)
    }

    @Test
    fun `buildSongFields keeps the year and the date independent`() {
        val date = LocalDate.of(1999, 5, 4)
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(date = date, year = 2001), artworkQualityMaxSide = null,
        )

        assertEquals(date, resolution.fields.date)
        assertEquals(2001, resolution.fields.year)
    }

    @Test
    fun `buildSongFields splits artists with the artist regex and genres with the genre regex`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(
                artists = setOf("A;B", "C"),
                albumArtists = setOf("A;B"),
                composers = setOf("D;E"),
                genres = setOf("Rock,Pop"),
            ),
            artworkQualityMaxSide = null,
        )

        assertEquals(setOf("A", "B", "C"), resolution.fields.artists)
        assertEquals(setOf("A", "B"), resolution.fields.albumArtists)
        assertEquals(setOf("D", "E"), resolution.fields.composers)
        assertEquals(setOf("Rock", "Pop"), resolution.fields.genres)
    }

    @Test
    fun `buildSongFields falls back to the file name when the title is missing`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(title = null), artworkQualityMaxSide = null,
        )

        assertEquals("Track", resolution.fields.title)
    }

    @Test
    fun `buildSongFields stores lyrics only when the source declares it`() {
        val metaphony = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(lyrics = "la la"), artworkQualityMaxSide = null,
        )
        val retriever = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(lyrics = "la la"), artworkQualityMaxSide = null,
        )

        assertEquals("la la", metaphony.lyricsToStore)
        assertNull(retriever.lyricsToStore)
    }

    @Test
    fun `buildSongFields copies the file facts into the fields`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = metaphonyLike,
            raw = rawMetadata(title = "T"), artworkQualityMaxSide = null,
        )

        assertEquals(1_700_000_000_000L, resolution.fields.dateModified)
        assertEquals(12_345L, resolution.fields.size)
        assertEquals("Music/Rock/Track.flac", resolution.fields.path)
    }

    @Test
    fun `buildSongFields plans a raw copy for unresized metaphony artwork`() {
        val picture = EmbeddedPicture(byteArrayOf(9), "image/png")

        val resolution = buildSongFields(
            fileInfo, options, id = "7", source = metaphonyLike,
            raw = rawMetadata(picture = picture), artworkQualityMaxSide = null,
        )

        assertEquals(CoverMode.RAW_COPY, resolution.coverPlan.mode)
        assertEquals("7.png", resolution.coverPlan.fileName)
        assertEquals(resolution.coverPlan.fileName, resolution.fields.coverFile)
    }

    @Test
    fun `buildSongFields plans a jpeg resize for retriever artwork`() {
        val picture = EmbeddedPicture(byteArrayOf(9), null)

        val resolution = buildSongFields(
            fileInfo, options, id = "7", source = retrieverLike,
            raw = rawMetadata(picture = picture), artworkQualityMaxSide = 512,
        )

        assertEquals(CoverMode.JPEG_RESIZE, resolution.coverPlan.mode)
        assertEquals("7.mr.jpg", resolution.coverPlan.fileName)
        assertEquals(resolution.coverPlan.fileName, resolution.fields.coverFile)
    }

    @Test
    fun `buildSongFields reports the source that produced the fields`() {
        val resolution = buildSongFields(
            fileInfo, options, id = "1", source = retrieverLike,
            raw = rawMetadata(title = "T"), artworkQualityMaxSide = null,
        )

        assertTrue(resolution.usedSource === retrieverLike)
    }
}

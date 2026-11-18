package io.github.zyrouge.symphony.services.groove

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.time.LocalDate

/**
 * JVM metadata reader over jaudiotagger. Policies mirror the Android metaphony
 * reader so a desktop scan produces the same song ids and cover file names.
 */
object JaudiotaggerMetadataSource : AudioMetadataSource {
    override val idSuffix: String = ""
    override val artworkPolicy: ArtworkPolicy = ArtworkPolicy(
        nameWithMimeExtension = true,
        dropWhenMimeUnknown = true,
        copyBytesWhenNoResize = true,
    )
    override val storesLyrics: Boolean = true
    override val durationIsSeconds: Boolean = true
    override val bitrateIsKilobits: Boolean = true
    override val failuresFallBack: Boolean = true

    override fun read(file: AudioFile): RawAudioMetadata? {
        val pathFile = file as PathAudioFile
        val audio = AudioFileIO.read(pathFile.path.toFile())
        val tag = runCatching { audio.tagOrCreateAndSetDefault }.getOrNull()
        val header = audio.audioHeader
        val year = tag.firstOrNull(FieldKey.YEAR)?.take(4)?.toIntOrNull()
        return RawAudioMetadata(
            title = tag.firstOrNull(FieldKey.TITLE),
            album = tag.firstOrNull(FieldKey.ALBUM),
            artists = tag.all(FieldKey.ARTIST),
            albumArtists = tag.all(FieldKey.ALBUM_ARTIST),
            composers = tag.all(FieldKey.COMPOSER),
            genres = tag.all(FieldKey.GENRE),
            trackNumber = tag.firstOrNull(FieldKey.TRACK)?.toIntOrNull(),
            trackTotal = tag.firstOrNull(FieldKey.TRACK_TOTAL)?.toIntOrNull(),
            discNumber = tag.firstOrNull(FieldKey.DISC_NO)?.toIntOrNull(),
            discTotal = tag.firstOrNull(FieldKey.DISC_TOTAL)?.toIntOrNull(),
            date = tag.firstOrNull(FieldKey.YEAR)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            year = year,
            durationRaw = header?.trackLength?.takeIf { it > 0 }?.toLong(),
            bitrateRaw = header?.bitRate?.trim()?.toLongOrNull()?.takeIf { it > 0 },
            samplingRate = header?.sampleRate?.trim()?.toLongOrNull(),
            channels = header?.channels?.trim()?.toIntOrNull(),
            encoder = header?.encodingType?.takeIf { it.isNotBlank() },
            lyrics = tag.firstOrNull(FieldKey.LYRICS),
            picture = runCatching { tag?.firstArtwork }
                .getOrNull()
                ?.let { EmbeddedPicture(it.binaryData, it.mimeType) },
        )
    }

    private fun Tag?.firstOrNull(key: FieldKey): String? =
        this?.getFirst(key)?.takeIf { it.isNotBlank() }

    private fun Tag?.all(key: FieldKey): Set<String> = this
        ?.getAll(key)
        ?.filter { it.isNotBlank() }
        ?.toSet()
        ?: emptySet()
}

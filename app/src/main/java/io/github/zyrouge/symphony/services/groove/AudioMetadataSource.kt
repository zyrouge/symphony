package io.github.zyrouge.symphony.services.groove

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import io.github.zyrouge.symphony.utils.DocumentFileX
import java.time.LocalDate
import me.zyrouge.symphony.metaphony.AudioMetadataParser

/**
 * The Android [AudioFile]: a SAF document plus the [Context] needed to open it.
 * Android metadata sources require this exact type.
 */
class SafAudioFile(
    val context: Context,
    val file: DocumentFileX,
    override val pathName: String,
) : AudioFile {
    override val name: String get() = file.name
    override val lastModified: Long get() = file.lastModified
    override val size: Long get() = file.size
}

object MetaphonyMetadataSource : AudioMetadataSource {
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
        val saf = file as SafAudioFile
        val metadata = saf.context.contentResolver
            .openFileDescriptor(saf.file.uri, "r")
            ?.use { AudioMetadataParser.parse(saf.file.name, it.detachFd()) }
            ?: return null
        return RawAudioMetadata(
            title = metadata.title,
            album = metadata.album,
            artists = metadata.artists,
            albumArtists = metadata.albumArtists,
            composers = metadata.composers,
            genres = metadata.genres,
            trackNumber = metadata.trackNumber,
            trackTotal = metadata.trackTotal,
            discNumber = metadata.discNumber,
            discTotal = metadata.discTotal,
            date = metadata.date,
            year = metadata.date?.year,
            durationRaw = metadata.lengthInSeconds?.toLong(),
            bitrateRaw = metadata.bitrate?.toLong(),
            samplingRate = metadata.sampleRate?.toLong(),
            channels = metadata.channels,
            encoder = metadata.encoding,
            lyrics = metadata.lyrics,
            picture = metadata.pictures.firstOrNull()
                ?.let { EmbeddedPicture(it.data, it.mimeType) },
        )
    }
}

object MediaMetadataRetrieverSource : AudioMetadataSource {
    override val idSuffix: String = ".mr"
    override val artworkPolicy: ArtworkPolicy = ArtworkPolicy(
        nameWithMimeExtension = false,
        dropWhenMimeUnknown = false,
        copyBytesWhenNoResize = false,
    )
    override val storesLyrics: Boolean = false
    override val durationIsSeconds: Boolean = false
    override val bitrateIsKilobits: Boolean = false
    override val failuresFallBack: Boolean = false

    override fun read(file: AudioFile): RawAudioMetadata? {
        val saf = file as SafAudioFile
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(saf.context, saf.file.uri)
        val samplingRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                ?.toLongOrNull()
        } else {
            null
        }
        return RawAudioMetadata(
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
            artists = listOfNotNull(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ).toSet(),
            albumArtists = listOfNotNull(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            ).toSet(),
            composers = listOfNotNull(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            ).toSet(),
            genres = listOfNotNull(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            ).toSet(),
            trackNumber = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.toIntOrNull(),
            trackTotal = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
                ?.toIntOrNull(),
            discNumber = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                ?.toIntOrNull(),
            discTotal = null,
            date = runCatching {
                LocalDate.parse(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                )
            }.getOrNull(),
            year = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull(),
            durationRaw = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull(),
            bitrateRaw = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toLongOrNull(),
            samplingRate = samplingRate,
            channels = null,
            encoder = null,
            lyrics = null,
            picture = retriever.embeddedPicture?.let { EmbeddedPicture(it, null) },
        )
    }
}

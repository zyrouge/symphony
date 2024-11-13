package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.AudioParser
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.ImagePreserver
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimplePath
import java.io.FileOutputStream
import java.math.RoundingMode
import java.time.LocalDate

@Immutable
@Entity("songs")
data class Song(
    @PrimaryKey
    val id: String,
    val title: String,
    val album: String?,
    val artists: Set<String>,
    val composers: Set<String>,
    val albumArtists: Set<String>,
    val genres: Set<String>,
    val trackNumber: Int?,
    val trackTotal: Int?,
    val discNumber: Int?,
    val discTotal: Int?,
    val date: LocalDate?,
    val year: Int?,
    val duration: Long,
    val bitrate: Long?,
    val minBitrate: Long?,
    val maxBitrate: Long?,
    val bitsPerSample: Int?,
    val samplingRate: Long?,
    val samples: Long?,
    val channels: Int?,
    val codec: String?,
    val encoder: String?,
    val dateModified: Long,
    val size: Long,
    val coverFile: String?,
    val uri: Uri,
    val path: String,
) {
    val variableBitrate get() = minBitrate != null && minBitrate != maxBitrate
    val bitrateK: Long? get() = bitrate?.let { it / 1000 }
    val minBitrateK: Long? get() = minBitrate?.let { it / 1000 }
    val maxBitrateK: Long? get() = maxBitrate?.let { it / 1000 }
    val samplingRateK: Float?
        get() = samplingRate?.let {
            (it.toFloat() / 1000)
                .toBigDecimal()
                .setScale(1, RoundingMode.CEILING)
                .toFloat()
        }

    val filename get() = SimplePath(path).name

    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.song.createArtworkImageRequest(id)

    fun toSamplingInfoString(symphony: Symphony): String? {
        val values = mutableListOf<String>()
        codec?.let {
            values.add(it)
        }
        channels?.let {
            values.add(symphony.t.XChannels(it.toString()))
        }
        bitsPerSample?.let {
            values.add(symphony.t.XBit(it.toString()))
        }
        bitrateK?.let {
            values.add(buildString {
                append(symphony.t.XKbps(it.toString()))
                if (variableBitrate) {
                    append(" (${symphony.t.VBR})")
                }
            })
        }
        samplingRateK?.let {
            values.add(symphony.t.XKHz(it.toString()))
        }
        return when {
            values.isNotEmpty() -> values.joinToString(", ")
            else -> null
        }
    }

    companion object {
        fun parse(symphony: Symphony, path: SimplePath, file: DocumentFileX): Song {
            if (symphony.settings.useMetaphony.value) {
                try {
                    val song = parseUsingMetaphony(symphony, path, file)
                    if (song != null) {
                        return song
                    }
                } catch (err: Exception) {
                    Logger.error("Song", "could not parse using metaphony", err)
                }
            }
            return parseUsingMediaMetadataRetriever(symphony, path, file)
        }

        private fun parseUsingMetaphony(
            symphony: Symphony,
            path: SimplePath,
            file: DocumentFileX,
        ): Song? {
            val parser = symphony.applicationContext.contentResolver.openInputStream(file.uri)
                ?.use { AudioParser.read(it, file.mimeType) }
                ?: return null
            val metadata = parser.getMetadata()
            val stream = parser.getStreamInfo()
            val id = symphony.groove.song.idGenerator.next()
            val coverFile = metadata.artworks.firstOrNull()?.let {
                if (it.format == AudioArtwork.Format.Unknown) {
                    return@let null
                }
                val quality = symphony.settings.artworkQuality.value
                if (quality.maxSide == null) {
                    val name = "$id.${it.format.extension}"
                    symphony.database.artworkCache.get(name).writeBytes(it.data)
                    return@let name
                }
                val bitmap = BitmapFactory.decodeByteArray(it.data, 0, it.data.size)
                val name = "$id.${AudioArtwork.Format.Jpeg.extension}"
                FileOutputStream(symphony.database.artworkCache.get(name)).use { writer ->
                    ImagePreserver
                        .resize(bitmap, quality)
                        .compress(Bitmap.CompressFormat.JPEG, 100, writer)
                }
                name
            }
            metadata.lyrics?.let {
                symphony.database.lyricsCache.put(id, it)
            }
            val artistSeparators = symphony.settings.artistTagSeparators.value
            val genreSeparators = symphony.settings.genreTagSeparators.value
            return Song(
                id = id,
                title = metadata.title ?: path.nameWithoutExtension,
                album = metadata.album,
                artists = parseMultiValue(metadata.artists, artistSeparators),
                composers = parseMultiValue(metadata.composer, artistSeparators),
                albumArtists = parseMultiValue(metadata.albumArtists, artistSeparators),
                genres = parseMultiValue(metadata.genres, genreSeparators),
                trackNumber = metadata.trackNumber,
                trackTotal = metadata.trackTotal,
                discNumber = metadata.discNumber,
                discTotal = metadata.discTotal,
                date = metadata.date,
                year = metadata.year,
                duration = stream.duration?.let { it * 1000 } ?: 0,
                bitrate = stream.bitrate,
                minBitrate = stream.minBitrate,
                maxBitrate = stream.maxBitrate,
                bitsPerSample = stream.bitsPerSample,
                samplingRate = stream.samplingRate,
                samples = stream.samples,
                channels = stream.channels,
                codec = stream.codec,
                encoder = metadata.encoder,
                dateModified = file.lastModified,
                size = file.size,
                coverFile = coverFile,
                uri = file.uri,
                path = path.pathString,
            )
        }

        fun parseUsingMediaMetadataRetriever(
            symphony: Symphony,
            path: SimplePath,
            file: DocumentFileX,
        ): Song {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(symphony.applicationContext, file.uri)
            val id = symphony.groove.song.idGenerator.next() + ".mr"
            val coverFile = retriever.embeddedPicture?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                val quality = symphony.settings.artworkQuality.value
                val name = "$id.${AudioArtwork.Format.Jpeg.extension}"
                FileOutputStream(symphony.database.artworkCache.get(name)).use { writer ->
                    ImagePreserver
                        .resize(bitmap, quality)
                        .compress(Bitmap.CompressFormat.JPEG, 100, writer)
                }
                name
            }
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val artists = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val composers = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            val albumArtists =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            val genres = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val trackNumber = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.toIntOrNull()
            val trackTotal = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
                ?.toIntOrNull()
            val discNumber = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                ?.toIntOrNull()
            val date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull()
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toLongOrNull()
            var bitsPerSample: Int? = null
            var samplingRate: Long? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bitsPerSample = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
                    ?.toIntOrNull()
                samplingRate = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                    ?.toLongOrNull()
            }
            val codec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                ?.let { getCodecFromMimetype(it) }
            val artistSeparators = symphony.settings.artistTagSeparators.value
            val genreSeparators = symphony.settings.genreTagSeparators.value
            return Song(
                id = id,
                title = title ?: path.nameWithoutExtension,
                album = album,
                artists = parseMultiValue(artists, artistSeparators),
                composers = parseMultiValue(composers, artistSeparators),
                albumArtists = parseMultiValue(albumArtists, artistSeparators),
                genres = parseMultiValue(genres, genreSeparators),
                trackNumber = trackNumber,
                trackTotal = trackTotal,
                discNumber = discNumber,
                discTotal = null,
                date = runCatching { LocalDate.parse(date) }.getOrNull(),
                year = year,
                duration = duration ?: 0,
                bitrate = bitrate,
                minBitrate = null,
                maxBitrate = null,
                bitsPerSample = bitsPerSample,
                samplingRate = samplingRate,
                samples = null,
                channels = null,
                codec = codec,
                encoder = null,
                dateModified = file.lastModified,
                size = file.size,
                coverFile = coverFile,
                uri = file.uri,
                path = path.pathString,
            )
        }

        fun parseMultiValue(value: String?, separators: Set<String>) = value?.let {
            parseMultiValue(setOf(it), separators)
        } ?: emptySet()

        fun parseMultiValue(values: Set<String>, separators: Set<String>): Set<String> {
            val result = mutableSetOf<String>()
            for (x in values) {
                val trimmed = x.trim()
                if (trimmed.isEmpty()) {
                    continue
                }
                result.addAll(trimmed.split(*separators.toTypedArray()))
            }
            return result
        }

        private val prettyCodecs = mapOf(
            "opus" to "Opus",
            "vorbis" to "Vorbis",
        )

        private fun getCodecFromMimetype(mimetype: String): String? {
            val codec = mimetype.lowercase().replaceFirst("audio/", "")
            return when {
                codec.isBlank() -> null
                else -> prettyCodecs[codec] ?: codec.uppercase()
            }
        }
    }
}

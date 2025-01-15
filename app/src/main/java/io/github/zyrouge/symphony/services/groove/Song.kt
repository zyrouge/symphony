package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.ImagePreserver
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimplePath
import me.zyrouge.symphony.metaphony.AudioMetadataParser
import java.io.FileOutputStream
import java.math.RoundingMode
import java.time.LocalDate
import java.util.regex.Pattern

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
    val samplingRate: Long?,
    val channels: Int?,
    val encoder: String?,
    val dateModified: Long,
    val size: Long,
    val coverFile: String?,
    val uri: Uri,
    val path: String,
) {
    data class ParseOptions(
        val symphony: Symphony,
        val artistSeparatorRegex: Regex,
        val genreSeparatorRegex: Regex,
    ) {
        companion object {
            fun create(symphony: Symphony) = ParseOptions(
                symphony = symphony,
                artistSeparatorRegex = makeSeparatorsRegex(symphony.settings.artistTagSeparators.value),
                genreSeparatorRegex = makeSeparatorsRegex(symphony.settings.genreTagSeparators.value),
            )
        }
    }

    val bitrateK: Long? get() = bitrate?.let { it / 1000 }
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
        encoder?.let {
            values.add(it)
        }
        channels?.let {
            values.add(symphony.t.XChannels(it.toString()))
        }
        bitrateK?.let {
            values.add(buildString {
                append(symphony.t.XKbps(it.toString()))
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
        fun parse(
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
        ): Song {
            if (options.symphony.settings.useMetaphony.value) {
                try {
                    val song = parseUsingMetaphony(path, file, options)
                    if (song != null) {
                        return song
                    }
                } catch (err: Exception) {
                    Logger.error("Song", "could not parse using metaphony", err)
                }
            }
            return parseUsingMediaMetadataRetriever(path, file, options)
        }

        private fun parseUsingMetaphony(
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
        ): Song? {
            val symphony = options.symphony
            val metadata = symphony.applicationContext.contentResolver
                .openFileDescriptor(file.uri, "r")
                ?.use { AudioMetadataParser.parse(file.name, it.detachFd()) }
                ?: return null
            val id = symphony.groove.song.idGenerator.next()
            val coverFile = metadata.pictures.firstOrNull()?.let {
                val extension = when (it.mimeType) {
                    "image/jpg", "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    else -> null
                }
                if (extension == null) {
                    return@let null
                }
                val quality = symphony.settings.artworkQuality.value
                if (quality.maxSide == null) {
                    val name = "$id.$extension"
                    symphony.database.artworkCache.get(name).writeBytes(it.data)
                    return@let name
                }
                val bitmap = BitmapFactory.decodeByteArray(it.data, 0, it.data.size)
                val name = "$id.jpg"
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
            return Song(
                id = id,
                title = metadata.title ?: path.nameWithoutExtension,
                album = metadata.album,
                artists = parseMultiValue(metadata.artists, options.artistSeparatorRegex),
                composers = parseMultiValue(metadata.composers, options.artistSeparatorRegex),
                albumArtists = parseMultiValue(metadata.albumArtists, options.artistSeparatorRegex),
                genres = parseMultiValue(metadata.genres, options.genreSeparatorRegex),
                trackNumber = metadata.trackNumber,
                trackTotal = metadata.trackTotal,
                discNumber = metadata.discNumber,
                discTotal = metadata.discTotal,
                date = metadata.date,
                year = metadata.date?.year,
                duration = metadata.lengthInSeconds?.let { it * 1000L } ?: 0,
                bitrate = metadata.bitrate?.let { it * 1000L },
                samplingRate = metadata.sampleRate?.toLong(),
                channels = metadata.channels,
                encoder = metadata.encoding,
                dateModified = file.lastModified,
                size = file.size,
                coverFile = coverFile,
                uri = file.uri,
                path = path.pathString,
            )
        }

        fun parseUsingMediaMetadataRetriever(
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
        ): Song {
            val symphony = options.symphony
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(symphony.applicationContext, file.uri)
            val id = symphony.groove.song.idGenerator.next() + ".mr"
            val coverFile = retriever.embeddedPicture?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                val quality = symphony.settings.artworkQuality.value
                val name = "$id.jpg"
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
            var samplingRate: Long? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                samplingRate = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                    ?.toLongOrNull()
            }
            return Song(
                id = id,
                title = title ?: path.nameWithoutExtension,
                album = album,
                artists = parseMultiValue(artists, options.artistSeparatorRegex),
                composers = parseMultiValue(composers, options.artistSeparatorRegex),
                albumArtists = parseMultiValue(albumArtists, options.artistSeparatorRegex),
                genres = parseMultiValue(genres, options.genreSeparatorRegex),
                trackNumber = trackNumber,
                trackTotal = trackTotal,
                discNumber = discNumber,
                discTotal = null,
                date = runCatching { LocalDate.parse(date) }.getOrNull(),
                year = year,
                duration = duration ?: 0,
                bitrate = bitrate,
                samplingRate = samplingRate,
                channels = null,
                encoder = null,
                dateModified = file.lastModified,
                size = file.size,
                coverFile = coverFile,
                uri = file.uri,
                path = path.pathString,
            )
        }

        private fun makeSeparatorsRegex(separators: Set<String>): Regex {
            val partial = separators.joinToString("|") { Pattern.quote(it) }
            return Regex("""(?<!\\)($partial)""")
        }

        fun parseMultiValue(value: String?, regex: Regex) = value?.let {
            parseMultiValue(setOf(it), regex)
        } ?: emptySet()

        fun parseMultiValue(values: Set<String>, regex: Regex): Set<String> {
            val result = mutableSetOf<String>()
            for (x in values) {
                for (y in x.trim().split(regex)) {
                    val trimmed = y.trim()
                    if (trimmed.isEmpty()) {
                        continue
                    }
                    result.add(trimmed)
                }
            }
            return result
        }
    }
}

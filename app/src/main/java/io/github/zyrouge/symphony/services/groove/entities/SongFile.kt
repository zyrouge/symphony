package io.github.zyrouge.symphony.services.groove.entities

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimplePath
import me.zyrouge.symphony.metaphony.AudioMetadataParser
import java.time.LocalDate
import java.util.regex.Pattern

@Immutable
@Entity(
    SongFile.TABLE,
)
data class SongFile(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_TITLE)
    val title: String,
    @ColumnInfo(COLUMN_ALBUM)
    val album: String?,
    @ColumnInfo(COLUMN_ARTISTS)
    val artists: Set<String>,
    @ColumnInfo(COLUMN_COMPOSERS)
    val composers: Set<String>,
    @ColumnInfo(COLUMN_ALBUM_ARTISTS)
    val albumArtists: Set<String>,
    @ColumnInfo(COLUMN_GENRES)
    val genres: Set<String>,
    @ColumnInfo(COLUMN_TRACK_NUMBER)
    val trackNumber: Int?,
    @ColumnInfo(COLUMN_TRACK_TOTAL)
    val trackTotal: Int?,
    @ColumnInfo(COLUMN_DISC_NUMBER)
    val discNumber: Int?,
    @ColumnInfo(COLUMN_DISC_TOTAL)
    val discTotal: Int?,
    @ColumnInfo(COLUMN_DATE)
    val date: LocalDate?,
    @ColumnInfo(COLUMN_YEAR)
    val year: Int?,
    @ColumnInfo(COLUMN_DURATION)
    val duration: Long,
    @ColumnInfo(COLUMN_BITRATE)
    val bitrate: Long?,
    @ColumnInfo(COLUMN_SAMPLING_RATE)
    val samplingRate: Long?,
    @ColumnInfo(COLUMN_CHANNELS)
    val channels: Int?,
    @ColumnInfo(COLUMN_ENCODER)
    val encoder: String?,
    @ColumnInfo(COLUMN_DATE_MODIFIED)
    val dateModified: Long,
    @ColumnInfo(COLUMN_SIZE)
    val size: Long,
    @ColumnInfo(COLUMN_URI)
    val uri: Uri,
    @ColumnInfo(COLUMN_PATH)
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

    data class Extended(
        val songFile: SongFile,
        val artwork: Artwork?,
        val lyrics: String?,
    ) {
        data class Artwork(val mimeType: String, val data: ByteArray)
    }

    companion object {
        const val TABLE = "song_files"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ALBUM = "album"
        const val COLUMN_ARTISTS = "artists"
        const val COLUMN_COMPOSERS = "composers"
        const val COLUMN_ALBUM_ARTISTS = "album_artists"
        const val COLUMN_GENRES = "genres"
        const val COLUMN_TRACK_NUMBER = "track_number"
        const val COLUMN_TRACK_TOTAL = "track_total"
        const val COLUMN_DISC_NUMBER = "disc_number"
        const val COLUMN_DISC_TOTAL = "disc_total"
        const val COLUMN_DATE = "date"
        const val COLUMN_YEAR = "year"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_BITRATE = "bitrate"
        const val COLUMN_SAMPLING_RATE = "sampling_rate"
        const val COLUMN_CHANNELS = "channels"
        const val COLUMN_ENCODER = "encoder"
        const val COLUMN_DATE_MODIFIED = "date_modified"
        const val COLUMN_SIZE = "size"
        const val COLUMN_URI = "uri"
        const val COLUMN_PATH = "path"

        fun parse(
            id: String,
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
        ): Extended {
            if (options.symphony.settings.useMetaphony.value) {
                try {
                    val songFile = parseUsingMetaphony(id, path, file, options)
                    if (songFile != null) {
                        return songFile
                    }
                } catch (err: Exception) {
                    Logger.error("SongFile", "could not parse using metaphony", err)
                }
            }
            return parseUsingMediaMetadataRetriever(id, path, file, options)
        }

        private fun parseUsingMetaphony(
            id: String,
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
        ): Extended? {
            val symphony = options.symphony
            val metadata = symphony.applicationContext.contentResolver
                .openFileDescriptor(file.uri, "r")
                ?.use { AudioMetadataParser.parse(file.name, it.detachFd()) }
                ?: return null
            val artwork = metadata.pictures.firstOrNull()?.let {
                Extended.Artwork(mimeType = it.mimeType, data = it.data)
            }
            val songFile = SongFile(
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
                uri = file.uri,
                path = path.pathString,
            )
            return Extended(songFile = songFile, artwork = artwork, lyrics = metadata.lyrics)
        }

        fun parseUsingMediaMetadataRetriever(
            id: String,
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
        ): Extended {
            val symphony = options.symphony
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(symphony.applicationContext, file.uri)
            val artwork = retriever.embeddedPicture?.let {
                Extended.Artwork(mimeType = "_", data = it)
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
            val songFile = SongFile(
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
                uri = file.uri,
                path = path.pathString,
            )
            return Extended(songFile = songFile, artwork = artwork, lyrics = null)
        }

        private fun makeSeparatorsRegex(separators: Set<String>): Regex {
            val partial = separators.joinToString("|") { Pattern.quote(it) }
            return Regex("""(?<!\\)($partial)""")
        }

        private fun parseMultiValue(value: String?, regex: Regex) = value?.let {
            parseMultiValue(setOf(it), regex)
        } ?: emptySet()

        private fun parseMultiValue(values: Set<String>, regex: Regex): Set<String> {
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
package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Immutable
import androidx.documentfile.provider.DocumentFile
import io.github.zyrouge.metaphony.Metadata
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.getIntOrNull
import io.github.zyrouge.symphony.utils.getStringOrEmptySet
import io.github.zyrouge.symphony.utils.getStringOrNull
import org.json.JSONObject
import java.math.RoundingMode
import kotlin.io.path.Path

@Immutable
data class Song(
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
    val year: Int?,
    val duration: Long,
    val bitrate: Int?,
    val bitsPerSample: Int?,
    val samplingRate: Int?,
    val codec: String?,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val coverFile: String?,
    val uri: Uri,
    val path: String,
) {
    val bitrateK: Int? get() = bitrate?.let { it / 1000 }
    val samplingRateK: Float?
        get() = samplingRate?.let {
            (it.toFloat() / 1000)
                .toBigDecimal()
                .setScale(1, RoundingMode.CEILING)
                .toFloat()
        }

    val filename = Path(path).fileName.toString()

    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.song.createArtworkImageRequest(id)

    fun toSamplingInfoString(symphony: Symphony): String? {
        val values = mutableListOf<String>()
        codec?.let { values.add(it) }
        bitsPerSample?.let {
            values.add(symphony.t.XBit(it.toString()))
        }
        bitrateK?.let {
            values.add(symphony.t.XKbps(it.toString()))
        }
        samplingRateK?.let {
            values.add(symphony.t.XKHz(it.toString()))
        }
        return when {
            values.isNotEmpty() -> values.joinToString(", ")
            else -> null
        }
    }

    fun toJSONObject() = JSONObject().apply {
        put(KEY_ID, id)
        put(KEY_TITLE, title)
        put(KEY_ALBUM, album)
        put(KEY_ARTISTS, artists)
        put(KEY_COMPOSERS, composers)
        put(KEY_ALBUM_ARTISTS, albumArtists)
        put(KEY_GENRES, genres)
        put(KEY_TRACK_NUMBER, trackNumber)
        put(KEY_TRACK_TOTAL, trackTotal)
        put(KEY_DISC_NUMBER, discNumber)
        put(KEY_DISC_TOTAL, discTotal)
        put(KEY_YEAR, year)
        put(KEY_DURATION, duration)
        put(KEY_BITRATE, bitrate)
        put(KEY_BITS_PER_SAMPLE, bitsPerSample)
        put(KEY_SAMPLING_RATE, samplingRate)
        put(KEY_CODEC, codec)
        put(KEY_DATE_ADDED, dateAdded)
        put(KEY_DATE_MODIFIED, dateModified)
        put(KEY_SIZE, size)
        put(KEY_COVER_FILE, coverFile)
        put(KEY_URI, uri)
        put(KEY_PATH, path)
    }

    companion object {
        const val KEY_TITLE = "0"
        const val KEY_ALBUM = "1"
        const val KEY_ARTISTS = "2"
        const val KEY_COMPOSERS = "3"
        const val KEY_ALBUM_ARTISTS = "4"
        const val KEY_GENRES = "5"
        const val KEY_TRACK_NUMBER = "6"
        const val KEY_TRACK_TOTAL = "7"
        const val KEY_DISC_NUMBER = "8"
        const val KEY_DISC_TOTAL = "9"
        const val KEY_YEAR = "10"
        const val KEY_DURATION = "11"
        const val KEY_BITRATE = "12"
        const val KEY_BITS_PER_SAMPLE = "13"
        const val KEY_SAMPLING_RATE = "14"
        const val KEY_CODEC = "15"
        const val KEY_DATE_ADDED = "16"
        const val KEY_DATE_MODIFIED = "17"
        const val KEY_SIZE = "18"
        const val KEY_URI = "19"
        const val KEY_PATH = "20"
        const val KEY_ID = "21"
        const val KEY_COVER_FILE = "22"

        fun fromJSONObject(json: JSONObject) = json.run {
            Song(
                id = getString(KEY_ID),
                title = getString(KEY_TITLE),
                album = getStringOrNull(KEY_ALBUM),
                artists = getStringOrEmptySet(KEY_ARTISTS),
                composers = getStringOrEmptySet(KEY_COMPOSERS),
                albumArtists = getStringOrEmptySet(KEY_ALBUM_ARTISTS),
                genres = getStringOrEmptySet(KEY_GENRES),
                trackNumber = getIntOrNull(KEY_TRACK_NUMBER),
                trackTotal = getIntOrNull(KEY_TRACK_TOTAL),
                discNumber = getIntOrNull(KEY_DISC_NUMBER),
                discTotal = getIntOrNull(KEY_DISC_TOTAL),
                year = getIntOrNull(KEY_YEAR),
                duration = getLong(KEY_DURATION),
                bitrate = getIntOrNull(KEY_BITRATE),
                bitsPerSample = getIntOrNull(KEY_BITS_PER_SAMPLE),
                samplingRate = getIntOrNull(KEY_SAMPLING_RATE),
                codec = getString(KEY_CODEC),
                dateAdded = getLong(KEY_DATE_ADDED),
                dateModified = getLong(KEY_DATE_MODIFIED),
                size = getLong(KEY_SIZE),
                coverFile = getStringOrNull(KEY_COVER_FILE),
                uri = Uri.parse(getString(KEY_URI)),
                path = getString(KEY_PATH),
            )
        }

        fun buildUri(id: Long) = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id,
        )

//        fun fromCursor(
//            symphony: Symphony,
//            shorty: CursorShorty,
//            fetchCachedAttributes: (Long) -> SongCache.Attributes?,
//        ): Song {
//            val artistSeparators = symphony.settings.artistTagSeparators.value
//            val id = shorty.getLong(AudioColumns._ID)
//            val dateModified = shorty.getLong(AudioColumns.DATE_MODIFIED)
//            return Song(
//                id = id,
//                title = shorty.getString(AudioColumns.TITLE),
//                trackNumber = shorty.getIntNullable(AudioColumns.TRACK)?.takeIf { it > 0 },
//                year = shorty.getIntNullable(AudioColumns.YEAR)?.takeIf { it > 0 },
//                duration = shorty.getLong(AudioColumns.DURATION),
//                album = shorty.getStringNullable(AudioColumns.ALBUM),
//                artists = shorty.getStringNullable(AudioColumns.ARTIST)
//                    ?.let { parseMultiValue(it, artistSeparators) } ?: setOf(),
//                composers = shorty.getStringNullable(AudioColumns.COMPOSER)
//                    ?.let { parseMultiValue(it, artistSeparators) } ?: setOf(),
//                dateAdded = shorty.getLong(AudioColumns.DATE_ADDED),
//                dateModified = dateModified,
//                size = shorty.getLong(AudioColumns.SIZE),
//                path = shorty.getString(AudioColumns.DATA),
//                additional = fetchCachedAttributes(id)
//                    ?.takeIf { it.lastModified == dateModified }
//                    ?.runCatching { AdditionalMetadata.fromSongCacheAttributes(symphony, this) }
//                    ?.getOrNull()
//                    ?: AdditionalMetadata.fetch(symphony, id),
//            )
//        }

        fun parse(symphony: Symphony, file: DocumentFile): Song? {
            val path = file.name!!
            val mimeType = file.type!!
            val uri = file.uri
            val metadata = symphony.applicationContext.contentResolver.openInputStream(uri)
                ?.use { Metadata.read(it, mimeType) }
                ?: return null
            val id = symphony.groove.song.coverIdGenerator.next()
            val coverFile = metadata.artworks.firstOrNull()?.let {
                val name = "$id.${it.format.extension}"
                symphony.database.artworkCache.get(name).writeBytes(it.data)
                name
            }
            metadata.lyrics?.let {
                symphony.database.lyricsCache.put(id, it)
            }
            return Song(
                id = id,
                title = metadata.title ?: path, // TODO
                album = metadata.album,
                artists = metadata.artists,
                composers = metadata.composer,
                albumArtists = metadata.albumArtists,
                genres = metadata.genres,
                trackNumber = metadata.trackNumber,
                trackTotal = metadata.trackTotal,
                discNumber = metadata.discNumber,
                discTotal = metadata.discTotal,
                year = metadata.year,
                duration = -1, // TODO
                bitrate = -1, // TODO
                bitsPerSample = -1, // TODO
                samplingRate = -1, // TODO
                codec = "", // TODO
                dateAdded = -1, // TODO
                dateModified = -1, // TODO
                size = -1, // TODO
                coverFile = coverFile,
                uri = uri,
                path = path,
            )
        }

        val prettyCodecs = mapOf(
            "opus" to "Opus",
            "vorbis" to "Vorbis",
        )

        fun prettyMimetype(mimetype: String): String? {
            val codec = mimetype.lowercase().replaceFirst("audio/", "")
            if (codec.isBlank()) {
                return null
            }
            return prettyCodecs[codec] ?: codec.uppercase()
        }

        fun parseMultiValue(value: String, separators: Set<String>) = value
            .split(*separators.toTypedArray())
            .mapNotNull { x -> x.trim().takeIf { it.isNotEmpty() } }
            .toSet()
    }
}

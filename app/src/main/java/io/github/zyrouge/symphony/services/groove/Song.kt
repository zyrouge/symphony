package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.documentfile.provider.DocumentFile
import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.AudioParser
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.RelaxedJsonDecoder
import io.github.zyrouge.symphony.utils.UriSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.RoundingMode
import kotlin.io.path.Path

@Immutable
@Serializable
data class Song(
    @SerialName(KEY_ID)
    val id: String,
    @SerialName(KEY_TITLE)
    val title: String,
    @SerialName(KEY_ALBUM)
    val album: String?,
    @SerialName(KEY_ARTISTS)
    val artists: Set<String>,
    @SerialName(KEY_COMPOSERS)
    val composers: Set<String>,
    @SerialName(KEY_ALBUM_ARTISTS)
    val albumArtists: Set<String>,
    @SerialName(KEY_GENRES)
    val genres: Set<String>,
    @SerialName(KEY_TRACK_NUMBER)
    val trackNumber: Int?,
    @SerialName(KEY_TRACK_TOTAL)
    val trackTotal: Int?,
    @SerialName(KEY_DISC_NUMBER)
    val discNumber: Int?,
    @SerialName(KEY_DISC_TOTAL)
    val discTotal: Int?,
    @SerialName(KEY_YEAR)
    val year: Int?,
    @SerialName(KEY_DURATION)
    val duration: Long,
    @SerialName(KEY_BITRATE)
    val bitrate: Long?,
    @SerialName(KEY_BITS_PER_SAMPLE)
    val bitsPerSample: Int?,
    @SerialName(KEY_SAMPLING_RATE)
    val samplingRate: Long?,
    @SerialName(KEY_CODEC)
    val codec: String?,
    @SerialName(KEY_DATE_MODIFIED)
    val dateModified: Long,
    @SerialName(KEY_SIZE)
    val size: Long,
    @SerialName(KEY_COVER_FILE)
    val coverFile: String?,
    @SerialName(KEY_URI)
    @Serializable(UriSerializer::class)
    val uri: Uri,
    @SerialName(KEY_PATH)
    val path: String,
) {
    val bitrateK: Long? get() = bitrate?.let { it / 1000 }
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

    fun toJson() = Json.encodeToString(this)

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
        const val KEY_DATE_MODIFIED = "17"
        const val KEY_SIZE = "18"
        const val KEY_URI = "19"
        const val KEY_PATH = "20"
        const val KEY_ID = "21"
        const val KEY_COVER_FILE = "22"

        fun fromJson(json: String) = RelaxedJsonDecoder.decodeFromString<Song>(json)

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
            val audio = symphony.applicationContext.contentResolver.openInputStream(uri)
                ?.use { AudioParser.read(it, mimeType) }
                ?: return null
            val metadata = audio.getMetadata()
            val stream = audio.getStreamInfo()
            val id = symphony.groove.song.idGenerator.next()
            val coverFile = metadata.artworks.firstOrNull()?.let {
                when (it.format) {
                    AudioArtwork.Format.Unknown -> null
                    else -> {
                        val name = "$id.${it.format.extension}"
                        symphony.database.artworkCache.get(name).writeBytes(it.data)
                        name
                    }
                }
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
                duration = stream.duration ?: 0,
                bitrate = stream.bitrate,
                bitsPerSample = stream.bitsPerSample,
                samplingRate = stream.samplingRate,
                codec = stream.codec,
                dateModified = file.lastModified(),
                size = file.length(),
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

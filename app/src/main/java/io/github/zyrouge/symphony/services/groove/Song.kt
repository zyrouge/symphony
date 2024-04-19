package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.CursorShorty
import java.math.RoundingMode
import kotlin.io.path.Path

@Immutable
data class Song(
    val id: Long,
    val title: String,
    val trackNumber: Int?,
    val year: Int?,
    val duration: Long,
    val album: String?,
    val artists: Set<String>,
    val composers: Set<String>,
    val additional: AdditionalMetadata,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val path: String,
) {
    @Immutable
    data class AdditionalMetadata(
        val albumArtists: Set<String>,
        val genres: Set<String>,
        val bitrate: Int?,
        val bitsPerSample: Int?,
        val samplingRate: Int?,
        val codec: String?,
    ) {
        val bitrateK: Int? get() = bitrate?.let { it / 1000 }
        val samplingRateK: Float?
            get() = samplingRate?.let {
                (it.toFloat() / 1000)
                    .toBigDecimal()
                    .setScale(1, RoundingMode.CEILING)
                    .toFloat()
            }

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

        companion object {
            fun fromSongCacheAttributes(
                symphony: Symphony,
                attributes: SongCache.Attributes,
            ): AdditionalMetadata {
                val artistSeparators = symphony.settings.artistTagSeparators.value
                val genreSeparators = symphony.settings.genreTagSeparators.value
                return AdditionalMetadata(
                    albumArtists = attributes.albumArtist
                        ?.let { parseMultiValue(it, artistSeparators) }
                        ?: setOf(),
                    bitrate = attributes.bitrate,
                    genres = attributes.genre
                        ?.let { parseMultiValue(it, genreSeparators) }
                        ?: setOf(),
                    bitsPerSample = attributes.bitsPerSample,
                    samplingRate = attributes.samplingRate,
                    codec = attributes.codec,
                )
            }

            fun fetch(symphony: Symphony, id: Long): AdditionalMetadata {
                var albumArtist: String? = null
                var bitrate: Int? = null
                var genre: String? = null
                var bitsPerSample: Int? = null
                var samplingRate: Int? = null
                var codec: String? = null
                kotlin.runCatching {
                    val retriever = MediaMetadataRetriever()
                    retriever.runCatching {
                        setDataSource(symphony.applicationContext, buildUri(id))
                        albumArtist =
                            extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                        bitrate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                            ?.toInt()
                        genre = extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            bitsPerSample =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
                                    ?.toInt()
                            samplingRate =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                                    ?.toInt()
                        }
                        codec = extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                            ?.let { prettyMimetype(it) }
                        retriever.release()
                    }
                }
                val artistSeparators = symphony.settings.artistTagSeparators.value
                val genreSeparators = symphony.settings.genreTagSeparators.value
                return AdditionalMetadata(
                    albumArtists = albumArtist
                        ?.let { parseMultiValue(it, artistSeparators) }
                        ?: setOf(),
                    bitrate = bitrate,
                    genres = genre
                        ?.let { parseMultiValue(it, genreSeparators) }
                        ?: setOf(),
                    bitsPerSample = bitsPerSample,
                    samplingRate = samplingRate,
                    codec = codec,
                )
            }
        }
    }

    val filename = Path(path).fileName.toString()
    val uri: Uri get() = buildUri(id)

    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.song.createArtworkImageRequest(id)

    companion object {
        fun buildUri(id: Long) = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id,
        )

        fun fromCursor(
            symphony: Symphony,
            shorty: CursorShorty,
            fetchCachedAttributes: (Long) -> SongCache.Attributes?,
        ): Song {
            val artistSeparators = symphony.settings.artistTagSeparators.value
            val id = shorty.getLong(AudioColumns._ID)
            val dateModified = shorty.getLong(AudioColumns.DATE_MODIFIED)
            return Song(
                id = id,
                title = shorty.getString(AudioColumns.TITLE),
                trackNumber = shorty.getIntNullable(AudioColumns.TRACK)?.takeIf { it > 0 },
                year = shorty.getIntNullable(AudioColumns.YEAR)?.takeIf { it > 0 },
                duration = shorty.getLong(AudioColumns.DURATION),
                album = shorty.getStringNullable(AudioColumns.ALBUM),
                artists = shorty.getStringNullable(AudioColumns.ARTIST)
                    ?.let { parseMultiValue(it, artistSeparators) } ?: setOf(),
                composers = shorty.getStringNullable(AudioColumns.COMPOSER)
                    ?.let { parseMultiValue(it, artistSeparators) } ?: setOf(),
                dateAdded = shorty.getLong(AudioColumns.DATE_ADDED),
                dateModified = dateModified,
                size = shorty.getLong(AudioColumns.SIZE),
                path = shorty.getString(AudioColumns.DATA),
                additional = fetchCachedAttributes(id)
                    ?.takeIf { it.lastModified == dateModified }
                    ?.runCatching { AdditionalMetadata.fromSongCacheAttributes(symphony, this) }
                    ?.getOrNull()
                    ?: AdditionalMetadata.fetch(symphony, id),
            )
        }

        val prettyCodecs = mapOf(
            "opus" to "Opus",
            "vorbis" to "Vorbis",
        )

        fun prettyMimetype(mimetype: String): String? {
            val codec = mimetype.lowercase().replaceFirst("audio/", "")
            if (codec.isBlank()) return null
            return prettyCodecs[codec] ?: codec.uppercase()
        }

        fun parseMultiValue(value: String, separators: Set<String>) = value
            .split(*separators.toTypedArray())
            .mapNotNull { x -> x.trim().takeIf { it.isNotEmpty() } }
            .toSet()
    }
}

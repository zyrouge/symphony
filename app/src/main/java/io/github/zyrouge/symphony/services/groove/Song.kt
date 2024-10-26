package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.AudioParser
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.SimplePath
import java.math.RoundingMode

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
    val year: Int?,
    val duration: Long,
    val bitrate: Long?,
    val bitsPerSample: Int?,
    val samplingRate: Long?,
    val codec: String?,
    val dateModified: Long,
    val size: Long,
    val coverFile: String?,
    val uri: Uri,
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

    val filename get() = SimplePath(path).name

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

    companion object {
        fun parse(symphony: Symphony, path: SimplePath, file: DocumentFileX): Song? {
            val audio = symphony.applicationContext.contentResolver.openInputStream(file.uri)
                ?.use { AudioParser.read(it, file.mimeType) }
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
                title = metadata.title ?: path.nameWithoutExtension, // TODO
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
                dateModified = file.lastModified,
                size = file.size,
                coverFile = coverFile,
                uri = file.uri,
                path = path.pathString,
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

package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimplePath
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
        val artworkSink: ArtworkSink,
    ) {
        companion object {
            fun create(symphony: Symphony) = ParseOptions(
                symphony = symphony,
                artistSeparatorRegex = makeSeparatorsRegex(symphony.settings.artistTagSeparators.value),
                genreSeparatorRegex = makeSeparatorsRegex(symphony.settings.genreTagSeparators.value),
                artworkSink = AndroidArtworkSink(symphony),
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
            val symphony = options.symphony
            val sources = if (symphony.settings.useMetaphony.value) {
                listOf(MetaphonyMetadataSource, MediaMetadataRetrieverSource)
            } else {
                listOf(MediaMetadataRetrieverSource)
            }
            val audioFile = SafAudioFile(
                context = symphony.applicationContext,
                file = file,
                pathName = path.pathString,
            )
            return readFirstAvailable(
                sources = sources,
                onSourceError = { _, err ->
                    Logger.error("Song", "could not parse using metaphony", err)
                },
            ) { source ->
                val metadata = source.read(audioFile)
                    ?: return@readFirstAvailable null
                assembleSong(path, file, options, source, metadata)
            } ?: error("no audio metadata source could read ${file.name}")
        }

        private fun assembleSong(
            path: SimplePath,
            file: DocumentFileX,
            options: ParseOptions,
            source: AudioMetadataSource,
            metadata: RawAudioMetadata,
        ): Song {
            val symphony = options.symphony
            val resolution = buildSongFields(
                fileInfo = FileInfo(
                    name = file.name,
                    lastModified = file.lastModified,
                    size = file.size,
                    pathName = path.pathString,
                ),
                options = ResolutionOptions(
                    artistSeparatorRegex = options.artistSeparatorRegex,
                    genreSeparatorRegex = options.genreSeparatorRegex,
                ),
                id = symphony.groove.song.idGenerator.next(),
                source = source,
                raw = metadata,
                artworkQualityMaxSide = symphony.settings.artworkQuality.value.maxSide,
            )
            val fields = resolution.fields
            resolution.lyricsToStore?.let {
                symphony.database.lyricsCache.put(fields.id, it)
            }
            return Song(
                id = fields.id,
                title = fields.title,
                album = fields.album,
                artists = fields.artists,
                composers = fields.composers,
                albumArtists = fields.albumArtists,
                genres = fields.genres,
                trackNumber = fields.trackNumber,
                trackTotal = fields.trackTotal,
                discNumber = fields.discNumber,
                discTotal = fields.discTotal,
                date = fields.date,
                year = fields.year,
                duration = fields.duration,
                bitrate = fields.bitrate,
                samplingRate = fields.samplingRate,
                channels = fields.channels,
                encoder = fields.encoder,
                dateModified = fields.dateModified,
                size = fields.size,
                coverFile = options.artworkSink.store(
                    resolution.coverPlan,
                    metadata.picture,
                ),
                uri = file.uri,
                path = fields.path,
            )
        }

    }
}

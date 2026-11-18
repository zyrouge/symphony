package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.utils.SimplePath
import java.time.LocalDate
import java.util.regex.Pattern

data class EmbeddedPicture(
    val data: ByteArray,
    val mimeType: String?,
)

/**
 * Metadata in the producing reader's own units.
 *
 * `durationRaw` and `bitrateRaw` are deliberately un-normalised: the reader
 * declares its units through [AudioMetadataSource.durationIsSeconds] and
 * [AudioMetadataSource.bitrateIsKilobits], and the resolver applies the single
 * conversion. See `docs/audio-metadata-invariants.md` INV-11.
 */
data class RawAudioMetadata(
    val title: String?,
    val album: String?,
    val artists: Set<String>,
    val albumArtists: Set<String>,
    val composers: Set<String>,
    val genres: Set<String>,
    val trackNumber: Int?,
    val trackTotal: Int?,
    val discNumber: Int?,
    val discTotal: Int?,
    val date: LocalDate?,
    val year: Int?,
    val durationRaw: Long?,
    val bitrateRaw: Long?,
    val samplingRate: Long?,
    val channels: Int?,
    val encoder: String?,
    val lyrics: String?,
    val picture: EmbeddedPicture?,
)

/**
 * Per-reader artwork behaviour. The differences are intentional (INV-7, INV-8,
 * INV-9) and belong here rather than inside a reader body.
 */
data class ArtworkPolicy(
    val nameWithMimeExtension: Boolean,
    val dropWhenMimeUnknown: Boolean,
    val copyBytesWhenNoResize: Boolean,
)

enum class CoverMode {
    RAW_COPY,
    JPEG_RESIZE,
    NONE,
}

data class CoverPlan(
    val fileName: String?,
    val mode: CoverMode,
)

/**
 * Everything a [Song] needs except its `uri`, which is Android-bound.
 */
data class SongFields(
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
    val path: String,
)

/**
 * One way of reading audio metadata. Implementations depend only on the
 * platform [AudioFile] they are built for, so host and device tests can drive
 * them without the app graph.
 */
interface AudioMetadataSource {
    val idSuffix: String

    val artworkPolicy: ArtworkPolicy

    val storesLyrics: Boolean

    val durationIsSeconds: Boolean

    val bitrateIsKilobits: Boolean

    /** True when a failure here falls through to the next source instead of propagating. */
    val failuresFallBack: Boolean

    fun read(file: AudioFile): RawAudioMetadata?
}

private const val JPEG_EXTENSION = "jpg"
private const val PNG_EXTENSION = "png"

fun secondsToMillisOrZero(seconds: Long?): Long = (seconds ?: 0L) * 1000L

fun kbpsToBps(kbps: Long?): Long? = kbps?.let { it * 1000L }

fun coverExtensionFor(mimeType: String?): String? = when (mimeType) {
    "image/jpg", "image/jpeg" -> JPEG_EXTENSION
    "image/png" -> PNG_EXTENSION
    else -> null
}

fun titleOrFileName(title: String?, path: SimplePath): String =
    title ?: path.nameWithoutExtension

fun shouldStoreLyrics(storesLyrics: Boolean, lyrics: String?): Boolean =
    storesLyrics && lyrics != null

/**
 * Decides the cover file name and how it is written (INV-7, INV-8, INV-9, INV-10).
 */
fun coverPlanFor(
    policy: ArtworkPolicy,
    picture: EmbeddedPicture?,
    artworkQualityMaxSide: Int?,
    id: String,
): CoverPlan {
    if (picture == null) {
        return CoverPlan(null, CoverMode.NONE)
    }

    if (policy.dropWhenMimeUnknown && coverExtensionFor(picture.mimeType) == null) {
        return CoverPlan(null, CoverMode.NONE)
    }

    val mode = if (policy.copyBytesWhenNoResize && artworkQualityMaxSide == null) {
        CoverMode.RAW_COPY
    } else {
        CoverMode.JPEG_RESIZE
    }

    val extension = if (mode == CoverMode.RAW_COPY && policy.nameWithMimeExtension) {
        coverExtensionFor(picture.mimeType) ?: JPEG_EXTENSION
    } else {
        JPEG_EXTENSION
    }

    return CoverPlan("$id.$extension", mode)
}

fun makeSeparatorsRegex(separators: Set<String>): Regex {
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

data class FileInfo(
    val name: String,
    val lastModified: Long,
    val size: Long,
    val pathName: String,
)

data class ResolutionOptions(
    val artistSeparatorRegex: Regex,
    val genreSeparatorRegex: Regex,
)

data class SongResolution(
    val fields: SongFields,
    val coverPlan: CoverPlan,
    val lyricsToStore: String?,
    val usedSource: AudioMetadataSource,
)

/**
 * Runs [attempt] against sources in order and returns the first non-null result.
 *
 * A failure falls through to the next source only when that source declares
 * [AudioMetadataSource.failuresFallBack]; otherwise it propagates, which is the retriever's
 * behaviour (INV-2, INV-3). [attempt] is deliberately whole-attempt: the original code wrapped
 * reading, artwork writing and lyrics caching for the metaphony path, so a failure in any of them
 * still fell back.
 */
fun <T : Any> readFirstAvailable(
    sources: List<AudioMetadataSource>,
    onSourceError: (AudioMetadataSource, Exception) -> Unit = { _, _ -> },
    attempt: (AudioMetadataSource) -> T?,
): T? {
    for (source in sources) {
        val result = if (source.failuresFallBack) {
            try {
                attempt(source)
            } catch (err: Exception) {
                onSourceError(source, err)
                null
            }
        } else {
            attempt(source)
        }
        if (result != null) {
            return result
        }
    }
    return null
}

fun buildSongFields(
    fileInfo: FileInfo,
    options: ResolutionOptions,
    id: String,
    source: AudioMetadataSource,
    raw: RawAudioMetadata,
    artworkQualityMaxSide: Int?,
): SongResolution {
    val songId = id + source.idSuffix
    val duration = if (source.durationIsSeconds) {
        secondsToMillisOrZero(raw.durationRaw)
    } else {
        raw.durationRaw ?: 0L
    }
    val bitrate = if (source.bitrateIsKilobits) {
        kbpsToBps(raw.bitrateRaw)
    } else {
        raw.bitrateRaw
    }
    val coverPlan = coverPlanFor(
        source.artworkPolicy,
        raw.picture,
        artworkQualityMaxSide,
        songId,
    )
    return SongResolution(
        fields = SongFields(
            id = songId,
            title = titleOrFileName(raw.title, SimplePath(fileInfo.pathName)),
            album = raw.album,
            artists = parseMultiValue(raw.artists, options.artistSeparatorRegex),
            composers = parseMultiValue(raw.composers, options.artistSeparatorRegex),
            albumArtists = parseMultiValue(raw.albumArtists, options.artistSeparatorRegex),
            genres = parseMultiValue(raw.genres, options.genreSeparatorRegex),
            trackNumber = raw.trackNumber,
            trackTotal = raw.trackTotal,
            discNumber = raw.discNumber,
            discTotal = raw.discTotal,
            date = raw.date,
            year = raw.year,
            duration = duration,
            bitrate = bitrate,
            samplingRate = raw.samplingRate,
            channels = raw.channels,
            encoder = raw.encoder,
            dateModified = fileInfo.lastModified,
            size = fileInfo.size,
            coverFile = coverPlan.fileName,
            path = fileInfo.pathName,
        ),
        coverPlan = coverPlan,
        lyricsToStore = if (shouldStoreLyrics(source.storesLyrics, raw.lyrics)) {
            raw.lyrics
        } else {
            null
        },
        usedSource = source,
    )
}

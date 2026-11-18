package io.github.zyrouge.symphony.services.groove

import android.content.Context
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.SimplePath
import java.time.LocalDate

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
 * One way of reading audio metadata. Implementations depend on a [Context] and a
 * [DocumentFileX] only, so host and device tests can drive them without the app graph.
 */
interface AudioMetadataSource {
    val idSuffix: String

    val artworkPolicy: ArtworkPolicy

    val storesLyrics: Boolean

    val durationIsSeconds: Boolean

    val bitrateIsKilobits: Boolean

    fun read(context: Context, file: DocumentFileX): RawAudioMetadata?
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

    val extension = if (policy.nameWithMimeExtension) {
        coverExtensionFor(picture.mimeType) ?: if (policy.dropWhenMimeUnknown) {
            return CoverPlan(null, CoverMode.NONE)
        } else {
            JPEG_EXTENSION
        }
    } else {
        JPEG_EXTENSION
    }

    val mode = if (policy.copyBytesWhenNoResize && artworkQualityMaxSide == null) {
        CoverMode.RAW_COPY
    } else {
        CoverMode.JPEG_RESIZE
    }

    return CoverPlan("$id.$extension", mode)
}

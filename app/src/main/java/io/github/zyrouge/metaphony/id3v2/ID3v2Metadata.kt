package io.github.zyrouge.metaphony.id3v2

import io.github.zyrouge.metaphony.Artwork
import io.github.zyrouge.metaphony.Metadata
import io.github.zyrouge.metaphony.utils.xDateToLocalDate
import io.github.zyrouge.metaphony.utils.xIntAfterSlash
import io.github.zyrouge.metaphony.utils.xIntBeforeSlash
import java.time.LocalDate

data class ID3v2Metadata(
    internal val rawTextDescFrames: Map<String, ID3v2Frames.ID3v2TextWithDescFrame>,
    internal val rawTextFrames: Map<String, Set<String>>,
    override val artworks: List<Artwork>,
) : Metadata {
    override val title: String? get() = textFrameSingle("TT2") ?: textFrameSingle("TIT2")
    override val artists: Set<String>
        get() = textFrameMultipleOrNull("TP1") ?: textFrameMultiple("TPE1")
    override val album: String? get() = textFrameSingle("TAL") ?: textFrameSingle("TALB")
    override val albumArtists: Set<String>
        get() = textFrameMultipleOrNull("TP2") ?: textFrameMultiple("TPE2")
    override val composer: Set<String>
        get() = textFrameMultipleOrNull("TCM") ?: textFrameMultiple("TCOM")
    override val genres: Set<String>
        get() = parseGenres()
    override val year: Int? get() = date?.year
    override val trackNumber: Int?
        get() = (textFrameSingle("TRK") ?: textFrameSingle("TRCK"))?.let {
            it.xIntBeforeSlash() ?: it.toIntOrNull()
        }
    override val trackTotal: Int?
        get() = (textFrameSingle("TRK") ?: textFrameSingle("TRCK"))?.xIntAfterSlash()
    override val discNumber: Int?
        get() = (textFrameSingle("TPA") ?: textFrameSingle("TPOS"))?.let {
            it.xIntBeforeSlash() ?: it.toIntOrNull()
        }
    override val discTotal: Int?
        get() = (textFrameSingle("TPA") ?: textFrameSingle("TPOS"))?.xIntAfterSlash()
    override val lyrics: String? get() = textFrameSingle("lyrics") ?: textFrameSingle("lyrics-xxx")
    override val comments: Set<String>
        get() = textFrameMultipleOrNull("COM") ?: textFrameMultiple("COMM")

    override val date: LocalDate?
        get() {
            val raw = textFrameSingle("TDRL") ?: return null
            if (raw.isEmpty()) return null
            return raw.xDateToLocalDate()
        }

    internal fun textFrameSingle(name: String) = rawTextFrames[name]?.firstOrNull()
    internal fun textFrameMultiple(name: String) = textFrameMultipleOrNull(name) ?: setOf()
    internal fun textFrameMultipleOrNull(name: String) = rawTextFrames[name]

    internal fun parseGenres(): Set<String> {
        val values = textFrameMultipleOrNull("TCO")
            ?: textFrameMultipleOrNull("TCON")
            ?: rawTextDescFrames.values.mapNotNull {
                when {
                    it.description.lowercase() == "genre" -> it.text.split(ID3v2Frames.ZERO_BYTE_CHARACTER)
                    else -> null
                }
            }.flatten().filter { it.isNotBlank() }.toSet()
        return ID3v2Genres.parseIDv2Genre(values)
    }

    internal data class Builder(
        val textDescFrames: MutableMap<String, ID3v2Frames.ID3v2TextWithDescFrame> = mutableMapOf(),
        val pictureFrames: MutableList<Artwork> = mutableListOf(),
        val textFrames: MutableMap<String, Set<String>> = mutableMapOf(),
    ) {
        fun done() = ID3v2Metadata(
            rawTextDescFrames = textDescFrames,
            rawTextFrames = textFrames,
            artworks = pictureFrames,
        )
    }
}
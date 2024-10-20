package io.github.zyrouge.metaphony.metadata.id3v2

import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.AudioMetadata
import io.github.zyrouge.metaphony.utils.xDateToLocalDateOrNull
import io.github.zyrouge.metaphony.utils.xIntAfterSlashOrNull
import io.github.zyrouge.metaphony.utils.xIntBeforeSlashOrIntOrNull

data class ID3v2Metadata(
    val textDescFrames: MutableMap<String, ID3v2Frames.ID3v2TextWithDescFrame> = mutableMapOf(),
    val pictureFrames: MutableList<AudioArtwork> = mutableListOf(),
    val textFrames: MutableMap<String, Set<String>> = mutableMapOf(),
) : AudioMetadata.Buildable {
    override fun title() = textFrame("TT2", "TIT2")
    override fun artists() = textFrames("TP1", "TPE1")
    override fun album() = textFrame("TAL", "TALB")
    override fun albumArtists() = textFrames("TP2", "TPE2")
    override fun composer() = textFrames("TCM", "TCOM")
    override fun trackNumber() = textFrame("TRK", "TRCK")?.xIntBeforeSlashOrIntOrNull()
    override fun trackTotal() = textFrame("TRK", "TRCK")?.xIntAfterSlashOrNull()
    override fun discNumber() = textFrame("TPA", "TPOS")?.xIntBeforeSlashOrIntOrNull()
    override fun discTotal() = textFrame("TPA", "TPOS")?.xIntAfterSlashOrNull()
    override fun lyrics() = textFrame("lyrics", "lyrics-xxx")
    override fun comments() = textFrames("COM", "COMM")
    override fun encoder() = null
    override fun date() = textFrame("TDRL")?.xDateToLocalDateOrNull()
    override fun year() = date()?.year
    override fun artworks() = pictureFrames

    override fun genres() = textFramesOrNull("TCO", "TCON")
        ?: textDescFrames.values
            .mapNotNull {
                if (it.description.lowercase() == "genre") it.text.split(ID3v2Frames.ZERO_BYTE_CHARACTER) else null
            }
            .flatten()
            .filter { it.isNotBlank() }
            .toSet()
            .let { ID3v2Genres.parseIDv2Genre(it) }

    private fun textFrame(vararg names: String) = textFrames(*names).firstOrNull()
    private fun textFrames(vararg names: String) = textFramesOrNull(*names) ?: setOf()
    private fun textFramesOrNull(vararg names: String) = names.firstNotNullOfOrNull {
        textFrames[it]
    }
}
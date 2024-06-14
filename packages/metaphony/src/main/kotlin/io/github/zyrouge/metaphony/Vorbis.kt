package io.github.zyrouge.metaphony

import java.io.InputStream
import java.time.LocalDate
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class VorbisMetadata(
    private val rawComments: Map<String, Set<String>>,
    override val artworks: List<Artwork>,
) : Metadata {
    override val title: String? get() = fieldSingle("title")
    override val artists: Set<String> get() = fieldMultiple("artist")
    override val album: String? get() = fieldSingle("album")
    override val albumArtists: Set<String>
        get() = fieldMultipleOrNull("albumartist") ?: fieldMultipleOrNull("album_artist") ?: setOf()
    override val composer: String? get() = fieldSingle("composer") ?: fieldSingle("performer") ?: fieldSingle("artist")
    override val genres: Set<String> get() = fieldMultiple("genre")
    override val year: Int? get() = fieldSingle("year")?.toInt() ?: date?.year
    override val trackNumber: Int? get() = fieldSingle("tracknumber")?.let { it.xIntBeforeSlash() ?: it.toInt() }
    override val trackTotal: Int?
        get() = fieldSingle("tracknumber")?.xIntAfterSlash() ?: fieldSingle("tracktotal")?.toInt()
    override val discNumber: Int? get() = fieldSingle("discnumber")?.let { it.xIntBeforeSlash() ?: it.toInt() }
    override val discTotal: Int?
        get() = fieldSingle("discnumber")?.xIntAfterSlash() ?: fieldSingle("disctotal")?.toInt()
    override val lyrics: String? get() = fieldSingle("lyrics") ?: fieldSingle("lyrics-xxx")
    override val comments: Set<String> get() = fieldMultiple("comment")

    override val date: LocalDate?
        get() {
            val raw = fieldSingle("date") ?: return null
            if (raw.isEmpty()) return null
            return parseDate(raw)
        }

    private fun fieldSingle(name: String) = rawComments[name]?.firstOrNull()
    private fun fieldMultiple(name: String) = fieldMultipleOrNull(name) ?: setOf()
    private fun fieldMultipleOrNull(name: String) = rawComments[name]

    internal data class Builder(
        val comments: MutableMap<String, Set<String>> = mutableMapOf(),
        val pictures: MutableList<Artwork> = mutableListOf(),
    ) {
        fun done() = VorbisMetadata(comments, pictures)
    }
}

@OptIn(ExperimentalEncodingApi::class)
internal fun VorbisMetadata.Builder.readVorbisComments(input: InputStream) {
    val vendorLen = input.xRead32bitLittleEndian()
    input.xSkipBytes(vendorLen)
    val commentsLen = input.xRead32bitLittleEndian()
    for (i in 0 until commentsLen) {
        val rawLen = input.xRead32bitLittleEndian()
        val raw = input.xReadString(rawLen)
        val (name, value) = parseVorbisComment(raw)
        when (name) {
            "metadata_block_picture" -> {
                Base64.decode(value).inputStream().use { pictureStream ->
                    readVorbisPicture(pictureStream)
                }
            }

            else -> comments.compute(name) { _, existing ->
                existing?.let { it + value } ?: setOf(value)
            }
        }
    }
}

private fun parseVorbisComment(raw: String): Pair<String, String> {
    val split = raw.split("=", limit = 2)
    if (split.size != 2) {
        throw Exception("Vorbis comment does not contain '='")
    }
    return split[0].lowercase() to split[1]
}

internal fun VorbisMetadata.Builder.readVorbisPicture(input: InputStream) {
    // skip picture type
    input.xSkipBytes(4)
    val mimeLen = input.xReadInt(4)
    val mime = input.xReadString(mimeLen)
    val format = Artwork.Format.fromMimeType(mime)
    val descLen = input.xReadInt(4)
    input.xSkipBytes(descLen)
    // skip width, height, color depth, color used
    input.xSkipBytes(4)
    input.xSkipBytes(4)
    input.xSkipBytes(4)
    input.xSkipBytes(4)
    val imageLen = input.xReadInt(4)
    val image = input.xReadBytes(imageLen)
    val artwork = Artwork(format = format, data = image)
    pictures.add(artwork)
}
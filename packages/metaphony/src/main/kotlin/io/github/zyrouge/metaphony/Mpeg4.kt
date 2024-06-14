package io.github.zyrouge.metaphony

import java.io.InputStream
import java.time.LocalDate

object Mpeg4 {
    fun read(input: InputStream): Mpeg4Metadata {
        val builder = Mpeg4Metadata.Builder()
        builder.readAtoms(input)
        return builder.done()
    }
}

data class Mpeg4Metadata(
    private val rawStringAtoms: Map<String, Set<String>>,
    private val rawUint8Atoms: Map<String, Int>,
    private val rawPictureAtoms: List<Artwork>,
) : Metadata {
    override val title: String? get() = stringAtomSingle("title")
    override val artists: Set<String> get() = stringAtomMultiple("artist")
    override val album: String? get() = stringAtomSingle("album")
    override val albumArtists: Set<String> get() = stringAtomMultiple("album_artist")
    override val composer: String? get() = stringAtomSingle("composer")
    override val genres: Set<String> get() = stringAtomMultiple("genre")
    override val year: Int? get() = date?.year
    override val trackNumber: Int? get() = uint8Atom("track")
    override val trackTotal: Int? get() = uint8Atom("track_total")
    override val discNumber: Int? get() = uint8Atom("disc")
    override val discTotal: Int? get() = uint8Atom("disc_total")
    override val lyrics: String? get() = stringAtomSingle("lyrics")
    override val comments: Set<String> get() = stringAtomMultiple("comment")
    override val artworks: List<Artwork> get() = rawPictureAtoms

    override val date: LocalDate?
        get() {
            val raw = stringAtomSingle("year") ?: return null
            if (raw.isEmpty()) return null
            return parseDate(raw)
        }

    private fun stringAtomSingle(name: String) = rawStringAtoms[name]?.firstOrNull()
    private fun stringAtomMultiple(name: String) = rawStringAtoms[name] ?: setOf()
    private fun uint8Atom(name: String) = rawUint8Atoms[name]

    internal data class Builder(
        val stringAtoms: MutableMap<String, Set<String>> = mutableMapOf(),
        val uint8Atoms: MutableMap<String, Int> = mutableMapOf(),
        val pictureAtoms: MutableList<Artwork> = mutableListOf(),
    ) {
        fun done() = Mpeg4Metadata(
            rawStringAtoms = stringAtoms,
            rawUint8Atoms = uint8Atoms,
            rawPictureAtoms = pictureAtoms,
        )
    }
}

// Source: https://atomicparsley.sourceforge.net/mpeg-4files.html
private val atomNames = mapOf(
    "©alb" to "album",
    "©art" to "artist",
    "aART" to "album_artist",
    "©cmt" to "comment",
    "©day" to "year",
    "©nam" to "title",
    "©gen" to "genre",
    "gnre" to "genre",
    "trkn" to "track",
    "disk" to "disc",
    "©wrt" to "composer",
    "covr" to "picture",
    "©lyr" to "lyrics",
)

private val dnsAtomNames = mapOf(
    "com.apple.iTunes:ARTISTS" to "artist",
)

private const val atomBinaryType = 0
private const val atomTextType = 1
private const val atomJpegType = 13
private const val atomPngType = 14
private const val atomUint8Type = 21

private fun Mpeg4Metadata.Builder.readAtoms(input: InputStream) {
    while (input.xAvailable()) {
        var (name, size) = readAtomHeader(input)
        if (name == "meta") {
            input.xSkipBytes(4)
            readAtoms(input)
            return
        }
        if (name == "moov" || name == "udta" || name == "ilst") {
            readAtoms(input)
            return
        }
        var canRead = false
        if (name == "----") {
            val (_, meanSize) = readAtomHeader(input)
            input.xSkipBytes(meanSize - 8)
            val (_, subSize) = readAtomHeader(input)
            input.xSkipBytes(4)
            val subName = input.xReadString(subSize - 12)
            name = dnsAtomNames[subName.substring(5)] ?: subName
            size -= meanSize + subSize
            canRead = true
        }
        atomNames[name]?.let {
            name = it
            canRead = true
        }
        if (!canRead) {
            input.xSkipBytes(size - 8)
            continue
        }
        readAtomData(input, name, size - 8)
    }
}

private fun readAtomHeader(input: InputStream): Pair<String, Int> {
    val size = input.xRead32bitBigEndian()
    val name = String(input.xReadBytes(4), Charsets.ISO_8859_1)
    return name to size
}

private fun Mpeg4Metadata.Builder.readAtomData(
    input: InputStream,
    name: String,
    size: Int
) {
    input.xSkipBytes(8)
    val contentType = input.xReadInt(4)
    input.xSkipBytes(4)
    val data = input.xReadBytes(size - 16)
    when (contentType) {
        atomBinaryType -> {
            if (name == "track" || name == "disc") {
                uint8Atoms[name] = data[3].xDecodeToUInt()
                uint8Atoms["${name}_total"] = data[5].xDecodeToUInt()
            }
        }

        atomTextType -> {
            val value = data.decodeToString()
            stringAtoms.compute(name) { _, old ->
                old?.let { it + value } ?: setOf(value)
            }
        }

        atomUint8Type -> {
            uint8Atoms[name] = data.xSlice(2).xDecodeToInt()
        }

        atomJpegType -> {
            val artwork = Artwork(
                format = Artwork.Format.fromMimeType("image/jpeg"),
                data = data,
            )
            pictureAtoms.add(artwork)
        }

        atomPngType -> {
            val artwork = Artwork(
                format = Artwork.Format.fromMimeType("image/png"),
                data = data,
            )
            pictureAtoms.add(artwork)
        }
    }
}

package io.github.zyrouge.metaphony

import java.io.InputStream
import java.time.LocalDate

data class ID3v2Metadata(
    private val rawTextDescFrames: Map<String, ID3v2TextWithDescFrame>,
    private val rawTextFrames: Map<String, Set<String>>,
    override val artworks: List<Artwork>,
) : Metadata {
    override val title: String? get() = textFrameSingle("TT2") ?: textFrameSingle("TIT2")
    override val artists: Set<String> get() = textFrameMultipleOrNull("TP1") ?: textFrameMultiple("TPE1")
    override val album: String? get() = textFrameSingle("TAL") ?: textFrameSingle("TALB")
    override val albumArtists: Set<String> get() = textFrameMultipleOrNull("TP2") ?: textFrameMultiple("TPE2")
    override val composer: String?
        get() = textFrameSingle("TCM") ?: textFrameSingle("TCOM")
    override val genres: Set<String>
        get() = parseGenres()
    override val year: Int? get() = date?.year
    override val trackNumber: Int?
        get() = (textFrameSingle("TRK") ?: textFrameSingle("TRCK"))?.let { it.xIntBeforeSlash() ?: it.toInt() }
    override val trackTotal: Int? get() = (textFrameSingle("TRK") ?: textFrameSingle("TRCK"))?.xIntAfterSlash()
    override val discNumber: Int?
        get() = (textFrameSingle("TPA") ?: textFrameSingle("TPOS"))?.let { it.xIntBeforeSlash() ?: it.toInt() }
    override val discTotal: Int?
        get() = (textFrameSingle("TPA") ?: textFrameSingle("TPOS"))?.xIntAfterSlash()
    override val lyrics: String? get() = textFrameSingle("lyrics") ?: textFrameSingle("lyrics-xxx")
    override val comments: Set<String> get() = textFrameMultipleOrNull("COM") ?: textFrameMultiple("COMM")

    override val date: LocalDate?
        get() {
            val raw = textFrameSingle("TDRL") ?: return null
            if (raw.isEmpty()) return null
            return parseDate(raw)
        }

    private fun textFrameSingle(name: String) = rawTextFrames[name]?.firstOrNull()
    private fun textFrameMultiple(name: String) = textFrameMultipleOrNull(name) ?: setOf()
    private fun textFrameMultipleOrNull(name: String) = rawTextFrames[name]

    private fun parseGenres(): Set<String> {
        val values = textFrameMultipleOrNull("TCO")
            ?: textFrameMultipleOrNull("TCON")
            ?: rawTextDescFrames.values.mapNotNull {
                when {
                    it.description.lowercase() == "genre" -> it.text.split(zeroByteCharacter)
                    else -> null
                }
            }.flatten().filter { it.isNotBlank() }.toSet()
        return parseIDv2Genre(values)
    }

    internal data class Builder(
        val textDescFrames: MutableMap<String, ID3v2TextWithDescFrame> = mutableMapOf(),
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

internal fun ID3v2Metadata.Builder.readID3v2Metadata(input: InputStream) {
    val header = readID3v2Header(input)
    while (input.xAvailable()) {
        readID3v2Frames(input, header)
    }
}

private enum class ID3v2Version {
    V2,
    V3,
    V4,
}

private data class Id3v2Header(
    val version: ID3v2Version,
    val size: Int,
    val unsynchronisation: Boolean,
    val offset: Int,
)

private fun readID3v2Header(input: InputStream): Id3v2Header {
    val marker = input.xReadString(3)
    if (marker != "ID3") {
        throw Exception("Missing marker")
    }
    val rawVersion = input.xReadInt(1)
    val version = when (rawVersion) {
        2 -> ID3v2Version.V2
        3 -> ID3v2Version.V3
        4 -> ID3v2Version.V4
        else -> throw Exception("Invalid version")
    }
    input.xSkipBytes(1)
    val flags = input.xReadByte()
    val unsynchronisation = flags.xBitSetAt(7)
    val extendedHeader = flags.xBitSetAt(6)
    val size = input.xReadInt(4, 7)
    var offset = 10
    if (extendedHeader) {
        val extendedHeaderSize = when (version) {
            ID3v2Version.V2 -> 0
            ID3v2Version.V3 -> input.xReadInt(4)
            ID3v2Version.V4 -> input.xReadInt(4, 7) - 4
        }
        input.xSkipBytes(extendedHeaderSize)
        offset += extendedHeaderSize
    }
    return Id3v2Header(
        version = version,
        size = size,
        unsynchronisation = unsynchronisation,
        offset = offset,
    )
}

private data class ID3v2FrameHeader(
    val name: String,
    val size: Int,
    val headerSize: Int,
)

private fun readID3v2r2FrameHeader(input: InputStream): ID3v2FrameHeader {
    val name = input.xReadString(3)
    val size = input.xReadInt(3)
    return ID3v2FrameHeader(
        name = name,
        size = size,
        headerSize = 6,
    )
}

private fun readID3v2r3FrameHeader(input: InputStream): ID3v2FrameHeader {
    val name = input.xReadString(4)
    val size = input.xReadInt(4)
    return ID3v2FrameHeader(
        name = name,
        size = size,
        headerSize = 8,
    )
}

private fun readID3v2r4FrameHeader(input: InputStream): ID3v2FrameHeader {
    val name = input.xReadString(4)
    val size = input.xReadInt(4, 7)
    return ID3v2FrameHeader(
        name = name,
        size = size,
        headerSize = 8,
    )
}

private data class ID3v2FrameFlags(
    val flagsSize: Int,
    val compression: Boolean,
    val encryption: Boolean,
    val unsynchronisation: Boolean,
    val dataLengthIndicator: Boolean,
)

private fun readID3v2r3FrameFlags(input: InputStream): ID3v2FrameFlags {
    input.xSkipBytes(1)
    val format = input.xReadByte()
    return ID3v2FrameFlags(
        flagsSize = 2,
        compression = format.xBitSetAt(7),
        encryption = format.xBitSetAt(6),
        unsynchronisation = false,
        dataLengthIndicator = false,
    )
}

private fun readID3v2r4FrameFlags(input: InputStream): ID3v2FrameFlags {
    input.xSkipBytes(1)
    val format = input.xReadByte()
    return ID3v2FrameFlags(
        flagsSize = 2,
        compression = format.xBitSetAt(3),
        encryption = format.xBitSetAt(2),
        unsynchronisation = format.xBitSetAt(1),
        dataLengthIndicator = format.xBitSetAt(0),
    )
}

private fun ID3v2Metadata.Builder.readID3v2Frames(input: InputStream, header: Id3v2Header) {
    var offset = header.offset
    while (offset < header.size) {
        val frameHeader = when (header.version) {
            ID3v2Version.V2 -> readID3v2r2FrameHeader(input)
            ID3v2Version.V3 -> readID3v2r3FrameHeader(input)
            ID3v2Version.V4 -> readID3v2r4FrameHeader(input)
        }
        val frameFlags = when (header.version) {
            ID3v2Version.V3 -> readID3v2r3FrameFlags(input)
            ID3v2Version.V4 -> readID3v2r4FrameFlags(input)
            else -> null
        }
        var size = frameHeader.size
        if (size == 0) break
        offset += frameHeader.headerSize + size
        frameFlags?.flagsSize?.let { offset += it }
        if (frameFlags?.compression == true) {
            when (header.version) {
                ID3v2Version.V3 -> {
                    input.xSkipBytes(4)
                    size -= 4
                }

                ID3v2Version.V4 -> {
                    size = input.xReadInt(4, 7)
                }

                else -> {}
            }
        }
        if (frameFlags?.encryption == true) {
            input.xSkipBytes(1)
            size--
        }
        val name = frameHeader.name
        val data = input.xReadBytes(size)
        // NOTE: not everything is parsed, only some needed ones
        when {
            name == "TXXX" || name == "TXX" -> {
                readTextDescFrame(data, hasLanguage = false, hasEncodedText = true).let {
                    textDescFrames[it.description] = it
                }
            }

            name.firstOrNull() == 'T' -> {
                textFrames[name] = readTFrame(data)
            }

            name == "WXXX" || name == "WXX" -> {
                readTextDescFrame(data, hasLanguage = false, hasEncodedText = false).let {
                    textDescFrames[it.description] = it
                }
            }

            name.firstOrNull() == 'W' -> {
                textFrames[name] = readWFrame(data)
            }

            name == "COMM" || name == "COM" || name == "USLT" || name == "ULT" -> {
                readTextDescFrame(data, hasLanguage = true, hasEncodedText = true).let {
                    textDescFrames[it.description] = it
                }
            }

            name == "APIC" -> {
                pictureFrames.add(readAPICFrame(data))
            }

            name == "PIC" -> {
                pictureFrames.add(readPICFrame(data))
            }
        }
    }
}

private const val textEncodingISO8859 = 0.toByte()
private const val textEncodingUTF16 = 1.toByte()
private const val textEncodingUTF16BE = 2.toByte()
private const val textEncodingUTF8 = 3.toByte()

private const val zeroByte = 0.toByte()
private val singleZeroDelimiter = byteArrayOf(zeroByte)
private val doubleZeroDelimiter = byteArrayOf(zeroByte, zeroByte)

private val zeroByteCharacter = String(singleZeroDelimiter)

private const val nullChar = 0.toChar()

data class ID3v2TextWithDescFrame(
    val language: String?,
    val description: String,
    val text: String,
)

private fun readTextDescFrame(
    data: ByteArray,
    hasLanguage: Boolean,
    hasEncodedText: Boolean,
): ID3v2TextWithDescFrame {
    var start = 1
    val encoding = data.first()
    val delimiter = when (encoding) {
        textEncodingUTF16, textEncodingUTF16BE -> doubleZeroDelimiter
        else -> singleZeroDelimiter
    }
    val language = when {
        hasLanguage -> {
            start += 3
            data.xSlice(to = 3).decodeToString()
        }

        else -> null
    }
    val info = data.xSlice(start).xSplit(delimiter, 2)
    val description = decodeText(encoding, info[0])
    val textEncoding = when {
        hasEncodedText -> encoding
        else -> textEncodingISO8859
    }
    val text = decodeText(textEncoding, info[1])
    return ID3v2TextWithDescFrame(
        language = language,
        text = text,
        description = description,
    )
}

private fun readTFrame(data: ByteArray): Set<String> {
    if (data.isEmpty()) return emptySet()
    val decoded = decodeText(data.first(), data.xSlice(1))
    val values = decoded.split(singleZeroDelimiter.decodeToString())
    return values.filter { it.isNotBlank() }.toSet()
}

private fun readWFrame(data: ByteArray) = readTFrame(byteArrayOf(0) + data)

private fun readAPICFrame(data: ByteArray): Artwork {
    val dataSplit = data.xSlice(1).xSplit(singleZeroDelimiter, 4)
    val mimeType = dataSplit.first().decodeToString()
    val bytes = dataSplit.last()
    return Artwork(
        format = Artwork.Format.fromMimeType(mimeType),
        data = bytes,
    )
}

private fun readPICFrame(data: ByteArray): Artwork {
    val mimeType = data.xSlice(1, 4).decodeToString()
    val descSplit = data.xSlice(5).xSplit(singleZeroDelimiter, 2)
    val bytes = descSplit.last()
    return Artwork(
        format = Artwork.Format.fromMimeType(mimeType),
        data = bytes,
    )
}

private fun decodeText(encoding: Byte, data: ByteArray): String {
    val charset = when (encoding) {
        textEncodingUTF16 -> Charsets.UTF_16
        textEncodingUTF16BE -> Charsets.UTF_16BE
        textEncodingUTF8 -> Charsets.UTF_8
        else -> Charsets.ISO_8859_1
    }
    return String(data, charset)
}

private val id3v2Genres = listOf(
    "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
    "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B",
    "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
    "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
    "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
    "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
    "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
    "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic",
    "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk",
    "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta",
    "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American",
    "Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes", "Trailer",
    "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro",
    "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock",
    "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival",
    "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
    "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band",
    "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
    "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus",
    "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba",
    "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle",
    "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall",
    "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
    "Britpop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
    "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
    "Christian Rock ", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop",
    "Synthpop",
    "Christmas", "Art Rock", "Baroque", "Bhangra", "Big Beat", "Breakbeat",
    "Chillout", "Downtempo", "Dub", "EBM", "Eclectic", "Electro",
    "Electroclash", "Emo", "Experimental", "Garage", "Global", "IDM",
    "Illbient", "Industro-Goth", "Jam Band", "Krautrock", "Leftfield", "Lounge",
    "Math Rock", "New Romantic", "Nu-Breakz", "Post-Punk", "Post-Rock", "Psytrance",
    "Shoegaze", "Space Rock", "Trop Rock", "World Music", "Neoclassical", "Audiobook",
    "Audio Theatre", "Neue Deutsche Welle", "Podcast", "Indie Rock", "G-Funk", "Dubstep",
    "Garage Rock", "Psybient",
)

private val id3v2GenreRegex = Regex.fromLiteral("""\d+""")

fun parseIDv2Genre(value: String): Set<String> {
    if (value[0] == '(') {
        val matches = id3v2GenreRegex.findAll(value)
        return matches.mapNotNull { id3v2Genres.getOrNull(it.value.toInt()) }.toSet()
    }
    value.toIntOrNull()?.let {
        val genre = id3v2Genres.getOrNull(it)
        return setOfNotNull(genre)
    }
    return value.split(nullChar).toSet()
}

fun parseIDv2Genre(values: Set<String>) = values.flatMap { parseIDv2Genre(it) }.toSet()

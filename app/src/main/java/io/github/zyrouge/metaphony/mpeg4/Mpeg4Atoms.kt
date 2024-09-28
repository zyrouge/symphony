package io.github.zyrouge.metaphony.mpeg4

import io.github.zyrouge.metaphony.Artwork
import io.github.zyrouge.metaphony.utils.xAvailable
import io.github.zyrouge.metaphony.utils.xDecodeToInt
import io.github.zyrouge.metaphony.utils.xDecodeToUInt
import io.github.zyrouge.metaphony.utils.xRead32bitBigEndian
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xSlice
import java.io.InputStream

object Mpeg4Atoms {
    private const val ATOM_BINARY_TYPE = 0
    private const val ATOM_TEXT_TYPE = 1
    private const val ATOM_JPEG_TYPE = 13
    private const val ATOM_PNG_TYPE = 14
    private const val ATOM_UNIT8_TYPE = 21

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

    internal fun Mpeg4Metadata.Builder.readAtoms(input: InputStream) {
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
        size: Int,
    ) {
        input.xSkipBytes(8)
        val contentType = input.xReadInt(4)
        input.xSkipBytes(4)
        val data = input.xReadBytes(size - 16)
        when (contentType) {
            ATOM_BINARY_TYPE -> {
                if (name == "track" || name == "disc") {
                    uint8Atoms[name] = data[3].xDecodeToUInt()
                    uint8Atoms["${name}_total"] = data[5].xDecodeToUInt()
                }
            }

            ATOM_TEXT_TYPE -> {
                val value = data.decodeToString()
                stringAtoms.compute(name) { _, old ->
                    old?.let { it + value } ?: setOf(value)
                }
            }

            ATOM_UNIT8_TYPE -> {
                uint8Atoms[name] = data.xSlice(2).xDecodeToInt()
            }

            ATOM_JPEG_TYPE -> {
                val artwork = Artwork(
                    format = Artwork.Format.fromMimeType("image/jpeg"),
                    data = data,
                )
                pictureAtoms.add(artwork)
            }

            ATOM_PNG_TYPE -> {
                val artwork = Artwork(
                    format = Artwork.Format.fromMimeType("image/png"),
                    data = data,
                )
                pictureAtoms.add(artwork)
            }
        }
    }
}
package io.github.zyrouge.metaphony.metadata.id3v2

import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xSlice
import io.github.zyrouge.metaphony.utils.xSplit
import java.io.InputStream

object ID3v2Frames {
    private const val TEXT_ENCODING_ISO_8859 = 0.toByte()
    private const val TEXT_ENCODING_UTF_16 = 1.toByte()
    private const val TEXT_ENCODING_UTF_16_BE = 2.toByte()
    private const val TEXT_ENCODING_UTF_8 = 3.toByte()

    private const val ZERO_BYTE = 0.toByte()
    private val SINGLE_ZERO_DELIMITER = byteArrayOf(ZERO_BYTE)
    private val DOUBLE_ZERO_DELIMITER = byteArrayOf(ZERO_BYTE, ZERO_BYTE)

    internal val ZERO_BYTE_CHARACTER = String(SINGLE_ZERO_DELIMITER)
    internal const val NULL_CHARACTER = 0.toChar()

    internal fun ID3v2Metadata.readID3v2Frames(input: InputStream) {
        val header = Id3v2Header.readID3v2Header(input) ?: return
        readID3v2Frames(input, header)
    }

    private fun ID3v2Metadata.readID3v2Frames(input: InputStream, header: Id3v2Header) {
        var offset = header.offset
        while (offset < header.size) {
            val frameHeader = ID3v2FrameHeader.readID3v2FrameHeader(input, header.version)
            val frameFlags = ID3v2FrameFlags.readID3v2FrameFlags(input, header.version)
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
            // NOTE: not everything is parsed, only some needed ones
            when {
                name == "TXXX" || name == "TXX" -> {
                    val data = input.xReadBytes(size)
                    readTextDescFrame(data, hasLanguage = false, hasEncodedText = true).let {
                        textDescFrames[it.description] = it
                    }
                }

                name.firstOrNull() == 'T' -> {
                    val data = input.xReadBytes(size)
                    textFrames[name] = readTFrame(data)
                }

                name == "WXXX" || name == "WXX" -> {
                    val data = input.xReadBytes(size)
                    readTextDescFrame(data, hasLanguage = false, hasEncodedText = false).let {
                        textDescFrames[it.description] = it
                    }
                }

                name.firstOrNull() == 'W' -> {
                    val data = input.xReadBytes(size)
                    textFrames[name] = readWFrame(data)
                }

                name == "COMM" || name == "COM" || name == "USLT" || name == "ULT" -> {
                    val data = input.xReadBytes(size)
                    readTextDescFrame(data, hasLanguage = true, hasEncodedText = true).let {
                        textDescFrames[it.description] = it
                    }
                }

                name == "APIC" -> {
                    val data = input.xReadBytes(size)
                    pictureFrames.add(readAPICFrame(data))
                }

                name == "PIC" -> {
                    val data = input.xReadBytes(size)
                    pictureFrames.add(readPICFrame(data))
                }

                else -> input.xSkipBytes(size)
            }
        }
    }

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
            TEXT_ENCODING_UTF_16, TEXT_ENCODING_UTF_16_BE -> DOUBLE_ZERO_DELIMITER
            else -> SINGLE_ZERO_DELIMITER
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
            else -> TEXT_ENCODING_ISO_8859
        }
        val text = decodeText(textEncoding, info[1])
        return ID3v2TextWithDescFrame(
            language = language,
            text = text,
            description = description,
        )
    }

    private fun readTFrame(data: ByteArray): Set<String> {
        if (data.isEmpty()) {
            return emptySet()
        }
        val decoded = decodeText(data.first(), data.xSlice(1))
        val values = decoded.split(SINGLE_ZERO_DELIMITER.decodeToString())
        return values.filter { it.isNotBlank() }.toSet()
    }

    private fun readWFrame(data: ByteArray) = readTFrame(byteArrayOf(0) + data)

    private fun readAPICFrame(data: ByteArray): AudioArtwork {
        val dataSplit = data.xSlice(1).xSplit(SINGLE_ZERO_DELIMITER, 3)
        val mimeType = dataSplit.first().decodeToString()
        val bytes = dataSplit.last()
        return AudioArtwork(
            format = AudioArtwork.Format.fromMimeType(mimeType),
            data = bytes,
        )
    }

    private fun readPICFrame(data: ByteArray): AudioArtwork {
        val mimeType = data.xSlice(1, 4).decodeToString()
        val descSplit = data.xSlice(5).xSplit(SINGLE_ZERO_DELIMITER, 2)
        val bytes = descSplit.last()
        return AudioArtwork(
            format = AudioArtwork.Format.fromMimeType(mimeType),
            data = bytes,
        )
    }

    private fun decodeText(encoding: Byte, data: ByteArray): String {
        val charset = when (encoding) {
            TEXT_ENCODING_UTF_16 -> Charsets.UTF_16
            TEXT_ENCODING_UTF_16_BE -> Charsets.UTF_16BE
            TEXT_ENCODING_UTF_8 -> Charsets.UTF_8
            else -> Charsets.ISO_8859_1
        }
        return String(data, charset)
    }
}
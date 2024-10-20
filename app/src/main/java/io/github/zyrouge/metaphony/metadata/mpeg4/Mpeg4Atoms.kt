package io.github.zyrouge.metaphony.metadata.mpeg4

import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.audio.mpeg4.Mpeg4StreamInfo
import io.github.zyrouge.metaphony.utils.xAvailable
import io.github.zyrouge.metaphony.utils.xDecodeToInt
import io.github.zyrouge.metaphony.utils.xDecodeToUInt
import io.github.zyrouge.metaphony.utils.xReadBEBuffer
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xSlice
import java.io.InputStream

class Mpeg4Atoms(val metadata: Mpeg4Metadata, val stream: Mpeg4StreamInfo) {
    fun readAtoms(input: InputStream) {
        while (input.xAvailable()) {
            var (name, size) = readAtomHeader(input)
            println(name)
            if (name == "meta") {
                input.xSkipBytes(4)
                readAtoms(input)
                return
            }
            if (name == "moov" || name == "udta" || name == "ilst" || name == "mdia" || name == "minf" || name == "stbl") {
                readAtoms(input)
                return
            }
            if (name == "stsd") {
                input.xSkipBytes(8)
                readAtoms(input)
                return
            }
            if (name == "mvhd") {
                readMvhdAtom(input, size)
                continue
            }
            if (name == "mp4a") {
                readMp4aAtom(input, size - 8)
                continue
            }
            if (name == "stts") {
                readSttsAtom(input, size - 8)
                continue
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
        val size = input.xReadBEBuffer(4).getInt()
        val name = String(input.xReadBytes(4), Charsets.ISO_8859_1).lowercase()
        return name to size
    }

    private fun readAtomData(input: InputStream, name: String, size: Int) {
        input.xSkipBytes(8)
        val contentType = input.xReadInt(4)
        input.xSkipBytes(4)
        val data = input.xReadBytes(size - 16)
        when (contentType) {
            ATOM_BINARY_TYPE -> {
                if (name == "track" || name == "disc") {
                    metadata.uint8Atoms[name] = data[3].xDecodeToUInt()
                    metadata.uint8Atoms["${name}_total"] = data[5].xDecodeToUInt()
                }
            }

            ATOM_TEXT_TYPE -> {
                val value = data.decodeToString()
                metadata.stringAtoms.compute(name) { _, old ->
                    old?.let { it + value } ?: setOf(value)
                }
            }

            ATOM_UNIT8_TYPE -> {
                metadata.uint8Atoms[name] = data.xSlice(2).xDecodeToInt()
            }

            ATOM_JPEG_TYPE -> {
                val artwork = AudioArtwork(
                    format = AudioArtwork.Format.fromMimeType("image/jpeg"),
                    data = data,
                )
                metadata.pictureAtoms.add(artwork)
            }

            ATOM_PNG_TYPE -> {
                val artwork = AudioArtwork(
                    format = AudioArtwork.Format.fromMimeType("image/png"),
                    data = data,
                )
                metadata.pictureAtoms.add(artwork)
            }
        }
    }

    private fun readMvhdAtom(input: InputStream, size: Int) {
        var n = size
        val version = input.xReadInt(1)
        input.xSkipBytes(3)
        n -= 4
        val (timeScale, timeLength) = when (version) {
            0 -> {
                input.xSkipBytes(8)
                val timeScale = input.xReadBEBuffer(4).getInt()
                val timeLength = input.xReadBEBuffer(4).getInt().toLong()
                n -= 16
                timeScale to timeLength
            }

            else -> {
                input.xSkipBytes(16)
                val timeScale = input.xReadBEBuffer(4).getInt()
                val timeLength = input.xReadBEBuffer(8).getLong()
                n -= 28
                timeScale to timeLength
            }
        }
        val duration = timeLength / timeScale
        stream.mDuration = duration
        println("$version to skip $n")
        input.xSkipBytes(n)
    }

    private fun readMp4aAtom(input: InputStream, size: Int) {
        var n = size
        input.xSkipBytes(16)
        n -= 16
        val channels = input.xReadBEBuffer(2).getShort().toInt()
        val bitsPerSample = input.xReadBEBuffer(2).getShort().toInt()
        input.xSkipBytes(4)
        val samplingRate = (input.xReadBEBuffer(4).getInt() shr 16).toUShort().toLong()
        n -= 12
        val bitrate = bitsPerSample * samplingRate * channels
        stream.mChannels = channels
        stream.mBitsPerSample = bitsPerSample
        stream.mSamplingRate = samplingRate
        stream.mBitrate = bitrate
        input.xSkipBytes(n)
    }

    private fun readSttsAtom(input: InputStream, size: Int) {
        var n = size
        input.xSkipBytes(4)
        val count = input.xReadBEBuffer(4).getInt()
        n -= 8
        var samples = 0L
        for (i in 0 until count) {
            samples += input.xReadBEBuffer(4).getInt()
            input.xSkipBytes(4)
            n -= 8
        }
        stream.mSamples = samples
        input.xSkipBytes(n)
    }

    companion object {
        const val ATOM_BINARY_TYPE = 0
        const val ATOM_TEXT_TYPE = 1
        const val ATOM_JPEG_TYPE = 13
        const val ATOM_PNG_TYPE = 14
        const val ATOM_UNIT8_TYPE = 21

        // Source: https://atomicparsley.sourceforge.net/mpeg-4files.html
        val atomNames = mapOf(
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
            "©too" to "encoder",
        )

        val dnsAtomNames = mapOf(
            "com.apple.iTunes:ARTISTS" to "artist",
        )
    }
}

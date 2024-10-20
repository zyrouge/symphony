package io.github.zyrouge.metaphony.audio.mp3

import io.github.zyrouge.metaphony.AudioStreamInfo
import io.github.zyrouge.metaphony.utils.xAvailable
import io.github.zyrouge.metaphony.utils.xReadBEBuffer
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import java.io.InputStream

class Mp3StreamInfo : AudioStreamInfo.Buildable {
    internal var mDuration: Long? = null
    internal var mBitrate: Long? = null
    internal var mMinBitrate: Long? = null
    internal var mMaxBitrate: Long? = null
    internal var mChannels: Int? = null
    internal var mBitsPerSample: Int? = null
    internal var mSamples: Long? = null
    internal var mSamplingRate: Long? = null
    internal var mCodec: String? = null

    override fun duration() = mDuration
    override fun bitrate() = mBitrate
    override fun minBitrate() = mMinBitrate
    override fun maxBitrate() = mMaxBitrate
    override fun channels() = mChannels
    override fun bitsPerSample() = mBitsPerSample
    override fun samples() = mSamples
    override fun samplingRate() = mSamplingRate
    override fun codec() = mCodec

    fun readStreamInfo(input: InputStream) {
        val part2 = skipToSync(input) ?: return
        val version = part2 and 0x18 shr 3
        val layer = part2 and 0x6 shr 1
        val protection = part2 and 0x1
        val part3 = input.xReadInt(1)
        val bitrateIndex = part3 shr 4
        val bitrate = getBitrate(version, layer, bitrateIndex)
        val samplingFreqIndex = part3 and 0xc shr 2
        val samplingRate = getSamplingRate(version, samplingFreqIndex)
        val samples = getSamples(version, layer)
        val part4 = input.xReadInt(1)
        val mode = part4 shr 6
        if (protection == 0) {
            input.xSkipBytes(2)
        }
        if (layer == LAYER_3) {
            input.xSkipBytes(getSideInfoLen(version, mode))
        }
        val tag = input.xReadString(4)
        val duration = when (tag) {
            "VBRI" -> {
                input.xSkipBytes(14)
                val totalFrame = input.xReadBEBuffer(4).getInt()
                totalFrame * samples / samplingRate
            }

            "Xing", "Info" -> {
                input.xSkipBytes(4)
                val totalFrame = input.xReadBEBuffer(4).getInt()
                totalFrame * samples / samplingRate
            }

            else -> null
        }
        mDuration = duration
        mBitrate = bitrate
        mMinBitrate = null
        mMaxBitrate = null
        mChannels = if (mode == SINGLE_CHANNEL) 1 else 2
        mBitsPerSample = null
        mSamples = samples
        mSamplingRate = samplingRate
        mCodec = mpegNames[version]
    }

    private fun skipToSync(input: InputStream): Int? {
        var hasPart1 = input.xReadInt(1) == 0xff
        while (input.xAvailable()) {
            val part2 = input.xReadInt(1)
            if (hasPart1 && part2 and 0xe0 == 0xe0) {
                return part2
            }
            hasPart1 = part2 == 0xff
        }
        return null
    }

    companion object {
        private const val MPEG_1 = 3
        private const val MPEG_2 = 2
        private const val MPEG_25 = 0

        private const val LAYER_1 = 3
        private const val LAYER_2 = 2
        private const val LAYER_3 = 1

        private const val STEREO = 0
        private const val JOINT_STEREO = 1
        private const val DUAL_CHANNEL = 2
        private const val SINGLE_CHANNEL = 3

        private val mpegNames = mapOf(
            MPEG_1 to "MPEG-1",
            MPEG_2 to "MPEG-2",
            MPEG_25 to "MPEG-2.5",
        )

        private val mpeg1BitrateTable = mapOf(
            LAYER_1 to listOf(
                0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 0
            ),
            LAYER_2 to listOf(0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, 0),
            LAYER_3 to listOf(0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 0),
        )
        private val mpeg2BitrateTable = mapOf(
            LAYER_1 to listOf(0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, 0),
            LAYER_2 to listOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 0),
            LAYER_3 to listOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 0),
        )

        private val mpeg1SampleTable = listOf(44100, 48000, 32000, 0)
        private val mpeg2SampleTable = listOf(22050, 24000, 16000, 0)
        private val mpeg25SampleTable = listOf(11025, 12000, 8000, 0)

        private fun getBitrate(version: Int, layer: Int, index: Int): Long {
            val bitrateK = when (version) {
                MPEG_1 -> mpeg1BitrateTable[layer]!![index]
                MPEG_2, MPEG_25 -> mpeg2BitrateTable[layer]!![index]
                else -> throw UnsupportedOperationException()
            }
            return bitrateK.toLong() * 1000
        }

        private fun getSamplingRate(version: Int, index: Int): Long {
            val samplingRateK = when (version) {
                MPEG_1 -> mpeg1SampleTable[index]
                MPEG_2 -> mpeg2SampleTable[index]
                MPEG_25 -> mpeg25SampleTable[index]
                else -> throw UnsupportedOperationException()
            }
            return samplingRateK.toLong()
        }

        private fun getSamples(version: Int, layer: Int): Long {
            val samples = when (layer) {
                LAYER_1 -> 384
                LAYER_2 -> 1152
                LAYER_3 -> when (version) {
                    MPEG_1 -> 1152
                    MPEG_2, MPEG_25 -> 576
                    else -> throw UnsupportedOperationException()
                }

                else -> throw UnsupportedOperationException()
            }
            return samples.toLong()
        }

        fun getSideInfoLen(version: Int, mode: Int) = when (mode) {
            STEREO, JOINT_STEREO, DUAL_CHANNEL -> when (version) {
                MPEG_1 -> 32
                MPEG_2, MPEG_25 -> 17
                else -> throw UnsupportedOperationException()
            }

            SINGLE_CHANNEL -> when (version) {
                MPEG_1 -> 17
                MPEG_2, MPEG_25 -> 9
                else -> throw UnsupportedOperationException()
            }

            else -> throw UnsupportedOperationException()
        }
    }
}

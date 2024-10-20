package io.github.zyrouge.metaphony.audio.flac

import io.github.zyrouge.metaphony.AudioStreamInfo
import io.github.zyrouge.metaphony.utils.xDecodeToLong
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xSlice
import java.io.InputStream
import kotlin.experimental.and

class FlacStreamInfo : AudioStreamInfo.Buildable {
    private var mDuration: Long? = null
    private var mBitrate: Long? = null
    private var mChannels: Int? = null
    private var mBitsPerSample: Int? = null
    private var mSamples: Long? = null
    private var mSamplingRate: Long? = null

    override fun duration() = mDuration
    override fun bitrate() = mBitrate
    override fun minBitrate() = null
    override fun maxBitrate() = null
    override fun channels() = mChannels
    override fun bitsPerSample() = mBitsPerSample
    override fun samples() = mSamples
    override fun samplingRate() = mSamplingRate
    override fun codec() = "FLAC"

    fun readVorbisStreamInfo(input: InputStream) {
        input.xSkipBytes(10)
        val info = input.xReadBytes(8)
        val samplingRate = info.xSlice(0, 3).xDecodeToLong() shr 4
        val channels = (info[2].toInt() and 0x0e shr 1) + 1
        val bitsPerSample = (info[2].toInt() and 0x01 shl 4) + (info[3].toInt() and 0xf0 shr 4) + 1
        val samples = info.xSlice(4).also {
            it[0] = it[0] and 0x0f
        }.xDecodeToLong()
        val duration = samples / samplingRate
        val bitrate = (samples * bitsPerSample * channels) / duration
        mDuration = duration
        mBitrate = bitrate
        mChannels = channels
        mBitsPerSample = bitsPerSample
        mSamples = samples
        mSamplingRate = samplingRate
        input.xSkipBytes(16)
    }
}
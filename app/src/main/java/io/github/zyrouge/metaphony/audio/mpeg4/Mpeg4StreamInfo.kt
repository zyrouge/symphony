package io.github.zyrouge.metaphony.audio.mpeg4

import io.github.zyrouge.metaphony.AudioStreamInfo

class Mpeg4StreamInfo : AudioStreamInfo.Buildable {
    internal var mDuration: Long? = null
    internal var mBitrate: Long? = null
    internal var mMinBitrate: Long? = null
    internal var mMaxBitrate: Long? = null
    internal var mChannels: Int? = null
    internal var mBitsPerSample: Int? = null
    internal var mSamples: Long? = null
    internal var mSamplingRate: Long? = null

    override fun duration() = mDuration
    override fun bitrate() = mBitrate
    override fun minBitrate() = mMinBitrate
    override fun maxBitrate() = mMaxBitrate
    override fun channels() = mChannels
    override fun bitsPerSample() = mBitsPerSample
    override fun samples() = mSamples
    override fun samplingRate() = mSamplingRate
    override fun codec() = "MPEG-4"
}

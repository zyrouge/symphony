package io.github.zyrouge.metaphony.audio.ogg

import io.github.zyrouge.metaphony.AudioStreamInfo
import io.github.zyrouge.metaphony.utils.xDecodeToLEBuffer
import io.github.zyrouge.metaphony.utils.xSlice

class OggStreamInfo : AudioStreamInfo.Buildable {
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
    override fun codec() = "Ogg"
}

fun OggStreamInfo.readOggPageHeader(header: OggPageHeader) {
    val samples = header.granulePosition.toLong()
    mSamples = samples
    mDuration = samples / mSamplingRate!!
}

fun OggStreamInfo.readVorbisComments(packet: ByteArray) {
    val channels = packet.xSlice(11, 12).xDecodeToLEBuffer().get().toInt()
    val samplingRate = packet.xSlice(12, 16).xDecodeToLEBuffer().getInt(0).toLong()
    val minBitrate = packet.xSlice(16, 20).xDecodeToLEBuffer().getInt(0).toLong().takeIf { it > 0 }
    val bitrate = packet.xSlice(20, 24).xDecodeToLEBuffer().getInt(0).toLong()
    val maxBitrate = packet.xSlice(24, 28).xDecodeToLEBuffer().getInt(0).toLong().takeIf { it > 0 }
    mChannels = channels
    mSamplingRate = samplingRate
    mBitrate = bitrate
    mMinBitrate = minBitrate
    mMaxBitrate = maxBitrate
}

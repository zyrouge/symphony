package io.github.zyrouge.metaphony

import androidx.compose.runtime.Immutable

@Immutable
data class AudioStreamInfo(
    val duration: Long?,
    val bitrate: Long?,
    val minBitrate: Long?,
    val maxBitrate: Long?,
    val channels: Int?,
    val bitsPerSample: Int?,
    val samplingRate: Long?,
    val samples: Long?,
    val codec: String?,
) {
    interface Buildable {
        fun duration(): Long?
        fun bitrate(): Long?
        fun minBitrate(): Long?
        fun maxBitrate(): Long?
        fun channels(): Int?
        fun bitsPerSample(): Int?
        fun samples(): Long?
        fun samplingRate(): Long?
        fun codec(): String?

        fun build() = AudioStreamInfo(
            duration = duration(),
            bitrate = bitrate(),
            minBitrate = minBitrate(),
            maxBitrate = maxBitrate(),
            channels = channels(),
            bitsPerSample = bitsPerSample(),
            samples = samples(),
            samplingRate = samplingRate(),
            codec = codec(),
        )
    }
}

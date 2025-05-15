package io.github.zyrouge.symphony.services.replayGain

import android.media.audiofx.LoudnessEnhancer
import io.github.zyrouge.symphony.Symphony
import kotlin.math.roundToInt

enum class ReplayGainNormalizationMode {
    AUTOMATIC,
    TRACK,
    ALBUM,
}

class ReplayGain(val symphony: Symphony) {
    private fun getGain(id: String): Float {
        if (!symphony.settings.replayGainEnabled.value) {
            return 0.0F
        }

        val preAmp = symphony.settings.replayGainPreAmp.value
        val song = symphony.groove.song.get(id) ?: return preAmp

        val songGain = when(symphony.settings.replayGainNormalizationMode.value) {
            ReplayGainNormalizationMode.AUTOMATIC -> {
                if(song.album == symphony.radio.queue.owningAlbum) {
                    song.replayGain.albumGain ?: song.replayGain.trackGain ?: 0.0F
                } else {
                    song.replayGain.trackGain ?: 0.0F
                }
            }
            ReplayGainNormalizationMode.TRACK -> song.replayGain.trackGain ?: 0.0F
            ReplayGainNormalizationMode.ALBUM -> song.replayGain.albumGain ?: song.replayGain.trackGain ?: 0.0F
        }

        return songGain + preAmp
    }

    /**
     * Applies replay gain based on settings and the replay gain information for [songId] to
     * the audio stream corresponding to [streamId].
     *
     * @return An opaque implementation that should be stored with the radio, to prevent garbage collection.
     */
    fun applyReplayGain(songId: String, streamId: Int): Any? {
        val gain = getGain(songId)
        if (gain == 0.0F) {
            return null
        }

        val effect = LoudnessEnhancer(streamId)
        effect.setTargetGain((gain * 100).roundToInt())
        effect.enabled = true
        return effect
    }
}

package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import kotlin.random.Random

class RadioShorty(private val symphony: Symphony) {
    fun playPause() {
        if (!symphony.radio.hasPlayer) return
        when {
            symphony.radio.isPlaying -> symphony.radio.pause()
            else -> symphony.radio.resume()
        }
    }

    fun seekFromCurrent(offsetSecs: Int) {
        if (!symphony.radio.hasPlayer) return
        symphony.radio.currentPlaybackPosition?.run {
            val to = (played + (offsetSecs * 1000)).coerceIn(0..total)
            symphony.radio.seek(to)
        }
    }

    fun previous(): Boolean {
        return when {
            !symphony.radio.hasPlayer -> false
            symphony.radio.currentPlaybackPosition!!.played <= 3000 && symphony.radio.canJumpToPrevious() -> {
                symphony.radio.jumpToPrevious()
                true
            }

            else -> {
                symphony.radio.seek(0)
                false
            }
        }
    }

    fun skip(): Boolean {
        if (!symphony.radio.hasPlayer) return false
        return when {
            !symphony.radio.hasPlayer -> false
            symphony.radio.canJumpToNext() -> {
                symphony.radio.jumpToNext()
                true
            }

            else -> {
                symphony.radio.play(Radio.PlayOptions(index = 0, autostart = false))
                false
            }
        }
    }

    fun playQueue(
        songIds: List<Long>,
        options: Radio.PlayOptions = Radio.PlayOptions(),
        shuffle: Boolean = false,
    ) {
        symphony.radio.stop(ended = false)
        if (songIds.isEmpty()) return
        symphony.radio.queue.add(
                songIds,
                options = options.run {
                    copy(index = if (shuffle) Random.nextInt(songIds.size) else options.index)
                }
        )
        symphony.radio.queue.setShuffleMode(shuffle)
    }

    fun playQueue(
        songId: Long,
        options: Radio.PlayOptions = Radio.PlayOptions(),
        shuffle: Boolean = false,
    ) = playQueue(listOf(songId), options = options, shuffle = shuffle)
}

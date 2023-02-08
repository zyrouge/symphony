package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import kotlin.random.Random

class RadioShorty(private val symphony: Symphony) {
    fun playPause() {
        if (!symphony.radio.hasPlayer) return
        when {
            symphony.radio.isPlaying -> symphony.radio.pause()
            else -> symphony.radio.resume()
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
        symphony.radio.queue.add(
            songIds,
            options = options.run {
                copy(index = if (shuffle) Random.nextInt(songIds.size) else options.index)
            }
        )
        symphony.radio.queue.setShuffleMode(shuffle)
    }

    @JvmName("playQueueFromSongList")
    fun playQueue(
        songs: List<Song>,
        options: Radio.PlayOptions = Radio.PlayOptions(),
        shuffle: Boolean = false,
    ) = playQueue(
        songIds = songs.map { it.id },
        options = options,
        shuffle = shuffle,
    )

    fun playQueue(song: Song, shuffle: Boolean = false) = playQueue(listOf(song), shuffle = shuffle)
}

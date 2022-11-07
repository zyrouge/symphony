package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song

class RadioShorty(private val symphony: Symphony) {
    fun playPause() {
        if (!symphony.radio.hasPlayer) return
        when {
            symphony.radio.isPlaying -> symphony.radio.pause()
            else -> symphony.radio.resume()
        }
    }

    fun previous() {
        if (!symphony.radio.hasPlayer) return
        if (symphony.radio.currentPlaybackPosition!!.played <= 3000 && symphony.radio.canJumpToPrevious()) {
            symphony.radio.jumpToPrevious()
        }
        symphony.radio.seek(0)
    }

    fun skip() {
        if (!symphony.radio.hasPlayer) return
        when {
            symphony.radio.canJumpToNext() -> symphony.radio.jumpToNext()
            else -> symphony.radio.play(Radio.PlayOptions(index = 0, autostart = false))
        }
    }

    fun playQueue(songs: List<Song>, options: Radio.PlayOptions = Radio.PlayOptions()) {
        symphony.radio.stop()
        symphony.radio.queue.add(songs, options = options)
    }

    fun playQueue(song: Song) = playQueue(listOf(song))
}

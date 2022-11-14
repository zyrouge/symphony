package io.github.zyrouge.symphony.services.radio

import java.util.*
import kotlin.math.max
import kotlin.math.min

object RadioEffects {
    class Fader(
        val options: Options,
        val onUpdate: (Float) -> Unit,
        val onEnd: () -> Unit,
    ) {
        data class Options(
            val from: Float,
            val to: Float,
            val duration: Long = DEFAULT_DURATION,
            val steps: Int = DEFAULT_STEPS,
        ) {
            companion object {
                private const val DEFAULT_DURATION = 1000L
                private const val DEFAULT_STEPS = 20
            }
        }

        private var timer: Timer? = null

        fun start() {
            val interval = options.duration / options.steps
            val increments = (options.to - options.from) / options.steps
            var volume = options.from
            val isReverse = options.to < options.from
            timer = kotlin.concurrent.timer(period = interval) {
                if (volume != options.to) {
                    onUpdate(volume)
                    volume = if (isReverse) max(options.to, volume + increments)
                    else min(options.to, volume + increments)
                } else {
                    onEnd()
                    destroy()
                }
            }
        }

        private fun destroy() {
            timer?.cancel()
            timer = null
        }
    }

    fun fadeIn(player: RadioPlayer, onEnd: () -> Unit) {
        val options = Fader.Options(
            when {
                player.isPlaying -> player.volume
                else -> RadioPlayer.MIN_VOLUME
            },
            RadioPlayer.MAX_VOLUME,
        )
        val fader = Fader(
            options,
            onUpdate = { player.setVolume(it) },
            onEnd = { onEnd() }
        )
        player.setVolume(options.from)
        player.start()
        fader.start()
    }

    fun fadeOut(player: RadioPlayer, onEnd: () -> Unit) {
        val options = Fader.Options(player.volume, RadioPlayer.MIN_VOLUME)
        val fader = Fader(
            options,
            onUpdate = { player.setVolume(it) },
            onEnd = {
                player.pause()
                onEnd()
            }
        )
        player.setVolume(options.from)
        fader.start()
    }
}

package io.github.zyrouge.symphony.services.radio

import java.util.Timer
import kotlin.math.max
import kotlin.math.min

object RadioEffects {
    class Fader(
        val options: Options,
        val onUpdate: (Float) -> Unit,
        val onFinish: (Boolean) -> Unit,
    ) {
        data class Options(
            val from: Float,
            val to: Float,
            val duration: Int,
            val interval: Int = DEFAULT_INTERVAL,
        ) {
            companion object {
                private const val DEFAULT_INTERVAL = 50
            }
        }

        private var timer: Timer? = null
        private var ended = false

        fun start() {
            val increments =
                (options.to - options.from) * (options.interval.toFloat() / options.duration)
            var volume = options.from
            val isReverse = options.to < options.from
            timer = kotlin.concurrent.timer(period = options.interval.toLong()) {
                if (volume != options.to) {
                    onUpdate(volume)
                    volume = when {
                        isReverse -> max(options.to, volume + increments)
                        else -> min(options.to, volume + increments)
                    }
                } else {
                    ended = true
                    onFinish(true)
                    destroy()
                }
            }
        }

        fun stop() {
            if (!ended) onFinish(false)
            destroy()
        }

        private fun destroy() {
            timer?.cancel()
            timer = null
        }
    }

//    fun fadeIn(player: RadioPlayer, onEnd: () -> Unit) {
//        val options = Fader.Options(
//            when {
//                player.isPlaying -> player.volume
//                else -> RadioPlayer.MIN_VOLUME
//            },
//            RadioPlayer.MAX_VOLUME,
//        )
//        val fader = Fader(
//            options,
//            onUpdate = { player.setVolume(it) },
//            onFinish = { onEnd() }
//        )
//        player.setVolume(options.from)
//        player.start()
//        fader.start()
//    }
//
//    fun fadeOut(player: RadioPlayer, onEnd: () -> Unit) {
//        val options = Fader.Options(player.volume, RadioPlayer.MIN_VOLUME)
//        val fader = Fader(
//            options,
//            onUpdate = { player.setVolume(it) },
//            onFinish = {
//                player.pause()
//                onEnd()
//            }
//        )
//        player.setVolume(options.from)
//        fader.start()
//    }
}

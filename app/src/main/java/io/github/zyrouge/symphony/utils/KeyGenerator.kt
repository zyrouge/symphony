package io.github.zyrouge.symphony.utils

import java.security.SecureRandom

interface KeyGenerator {
    fun next(): String

    class TimeCounterRandomMix : KeyGenerator {
        private var time = System.currentTimeMillis()
        private var i = -1
        private val random = SecureRandom()

        @Synchronized
        override fun next(): String {
            val suffix = random.nextInt(9999)
            // 256 for the giggles
            if (i < 256) {
                i++
                return "$time#$i#$suffix"
            }
            val now = System.currentTimeMillis()
            if (now <= time) {
                i++
                return "$time#$i#$suffix"
            }
            time = now
            i = 0
            return "$now#0#$suffix"
        }
    }
}

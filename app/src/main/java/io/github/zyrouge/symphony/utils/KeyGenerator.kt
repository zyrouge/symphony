package io.github.zyrouge.symphony.utils

interface KeyGenerator {
    fun next(): String

    class TimeIncremental : KeyGenerator {
        private var time = System.currentTimeMillis()
        private var i = -1

        @Synchronized
        override fun next(): String {
            // 256 for the giggles
            if (i < 256) {
                i++
                return "$time#$i"
            }
            val now = System.currentTimeMillis()
            if (now <= time) {
                i++
                return "$time#$i"
            }
            time = now
            i = 0
            return "$now#0"
        }
    }
}

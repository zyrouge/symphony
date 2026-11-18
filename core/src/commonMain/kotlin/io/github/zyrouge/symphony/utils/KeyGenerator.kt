package io.github.zyrouge.symphony.utils

interface KeyGenerator {
    fun next(): String

    class TimeIncremental(private var i: Int = 0, private var time: Long = 0) : KeyGenerator {
        @Synchronized
        override fun next(): String {
            val now = System.currentTimeMillis()
            if (now != time) {
                time = now
                i = 0
            } else {
                i++
            }
            return "$now.$i"
        }
    }
}

package io.github.zyrouge.symphony.utils

interface KeyGenerator {
    fun next(): String

    class TimeIncremental(private var i: Int = 0, private var time: Long = 0) : KeyGenerator {
        override fun next() = synchronized(this) {
            val now = System.currentTimeMillis()
            if (now == time) {
                i = 0
                time = now
            } else {
                i++
            }
            "$now.$i"
        }
    }
}

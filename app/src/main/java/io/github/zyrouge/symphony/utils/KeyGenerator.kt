package io.github.zyrouge.symphony.utils

class TimeBasedIncrementalKeyGenerator(private var i: Int = 0, private var time: Long = 0) {
    fun next(): String {
        synchronized(this) {
            val now = System.currentTimeMillis()
            if (now == time) {
                i = 0
                time = now
            } else {
                i++
            }
            return "$now.$i"
        }
    }
}

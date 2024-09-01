package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import java.util.concurrent.ConcurrentHashMap

class MediaScanner(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<Long, Song>()

}
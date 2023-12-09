package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.AudioTaggerX
import io.github.zyrouge.symphony.utils.FileX
import io.github.zyrouge.symphony.utils.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class LyricsRepository(private val symphony: Symphony) {
    private val compactCacheKeys = ConcurrentLinkedQueue<String>()
    val cache = ConcurrentHashMap<String, String>()

    fun fetch() {
        kotlin
            .runCatching { symphony.database.lyricsCache.read() }
            .getOrNull()
            ?.let {
                compactCacheKeys.addAll(it.keys)
                cache.putAll(it)
            }
    }

    fun reset() {
        cache.clear()
    }

    suspend fun getLyrics(song: Song): String? {
        val cacheKey = constructSongCacheKey(song)
        cache[cacheKey]?.let { lyrics ->
            hitCompactCache(cacheKey)
            return lyrics
        }
        try {
            val outputFile = symphony.applicationContext.cacheDir
                .toPath()
                .resolve(song.filename)
                .toFile()
            FileX.ensureFile(outputFile)
            symphony.applicationContext.contentResolver.openInputStream(song.uri)
                ?.use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            val lyrics = AudioTaggerX.getLyrics(outputFile)
            outputFile.delete()
            lyrics?.let {
                cache[cacheKey] = lyrics
                hitCompactCache(cacheKey)
                symphony.database.lyricsCache.update(getCompactCache())
            }
            return lyrics
        } catch (err: Exception) {
            Logger.error("LyricsRepository", "fetch lyrics failed", err)
        }
        return null
    }

    private fun hitCompactCache(cacheKey: String) {
        while (compactCacheKeys.size > MAX_CACHE_SIZE) {
            compactCacheKeys.remove()
        }
        compactCacheKeys.add(cacheKey)
    }

    private fun getCompactCache(): Map<String, String> {
        val output = mutableMapOf<String, String>()
        compactCacheKeys.forEach { cacheKey ->
            cache[cacheKey]?.let { lyrics ->
                output[cacheKey] = lyrics
            }
        }
        return output
    }

    companion object {
        const val MAX_CACHE_SIZE = 50

        fun constructSongCacheKey(song: Song) = "${song.id}-${song.dateModified}"
    }
}

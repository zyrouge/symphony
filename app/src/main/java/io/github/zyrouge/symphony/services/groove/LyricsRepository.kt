package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.AudioTaggerX
import io.github.zyrouge.symphony.utils.FileX
import io.github.zyrouge.symphony.utils.Logger
import java.util.concurrent.ConcurrentHashMap

class LyricsRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<Long, String>()
    val previousSaveCache = ConcurrentHashMap<String, String>()
    val currentSaveCache = ConcurrentHashMap<String, String>()

    fun ready() {
        symphony.groove.mediaStore.onSong.subscribe { onSong(it) }
        symphony.groove.mediaStore.onFetchStart.subscribe { onFetchStart() }
        symphony.groove.mediaStore.onFetchEnd.subscribe { onFetchEnd() }
    }

    private fun onFetchStart() {
        kotlin
            .runCatching { symphony.database.lyricsCache.read() }
            .getOrNull()
            ?.let { previousSaveCache.putAll(it) }
    }

    private fun onFetchEnd() {
        previousSaveCache.clear()
        symphony.database.lyricsCache.update(currentSaveCache)
    }

    private fun onSong(song: Song) {
        val saveCacheKey = constructSongCacheKey(song)
        previousSaveCache[saveCacheKey]?.let { lyrics ->
            cache[song.id] = lyrics
            currentSaveCache[saveCacheKey] = lyrics
        }
    }

    fun reset() {
        cache.clear()
        previousSaveCache.clear()
        currentSaveCache.clear()
    }

    suspend fun getLyrics(song: Song): String? = run {
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
                cache[song.id] = lyrics
                currentSaveCache[constructSongCacheKey(song)] = lyrics
                symphony.database.lyricsCache.update(currentSaveCache.toMap())
            }
            return lyrics
        } catch (err: Exception) {
            Logger.error("LyricsRepository", "fetch lyrics failed", err)
        }
        return null
    }

    fun constructSongCacheKey(song: Song) = "${song.id}-${song.dateModified}"
}

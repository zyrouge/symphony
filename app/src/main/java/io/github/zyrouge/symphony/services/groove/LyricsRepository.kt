package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.AudioTaggerX
import io.github.zyrouge.symphony.utils.CursorShorty
import io.github.zyrouge.symphony.utils.FileX
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.getColumnIndices
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

private data class LrcFile(val id: Long, val dateModified: Long, val path: String)

class LyricsRepository(private val symphony: Symphony) {
    private val lrcFiles = ConcurrentHashMap<String, LrcFile>()
    private val compactCacheKeys = ConcurrentLinkedQueue<String>()
    val cache = ConcurrentHashMap<String, String>()

    fun fetch() {
        try {
            kotlin
                .runCatching { symphony.database.lyricsCache.read() }
                .getOrNull()
                ?.let {
                    compactCacheKeys.addAll(it.keys)
                    cache.putAll(it)
                }
        } catch (err: Exception) {
            Logger.error("LyricsRepository", "loading cache failed", err)
        }
        try {
            val cursor = symphony.applicationContext.contentResolver.query(
                contentUri,
                projectedColumns.toTypedArray(),
                MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_SUBTITLE,
                null,
                null
            )
            cursor?.use {
                val shorty = CursorShorty(it, it.getColumnIndices(projectedColumns))
                while (it.moveToNext()) {
                    val id = shorty.getLong(MediaStore.Files.FileColumns._ID)
                    val dateModified = shorty.getLong(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    val path = shorty.getString(MediaStore.Files.FileColumns.DATA)
                    if (!path.endsWith(".lrc")) continue
                    lrcFiles[path] = LrcFile(id = id, dateModified = dateModified, path = path)
                }
            }
        } catch (err: Exception) {
            Logger.error("LyricsRepository", "mediastore failed", err)
        }
    }

    fun reset() {
        cache.clear()
    }

    suspend fun getLyrics(song: Song): String? {
        val lrcFilePath = constructLrcPathFromSong(song)
        lrcFiles[lrcFilePath]?.let { lrcFile ->
            val cacheKey = constructCacheKey(lrcFile.id, lrcFile.dateModified, "lrc-")
            cache[cacheKey]?.let { lyrics ->
                hitCompactCache(cacheKey)
                return lyrics
            }
            val uri = buildUri(lrcFile.id)
            symphony.applicationContext.contentResolver
                .openInputStream(uri)
                ?.use { inputStream ->
                    val lyrics = String(inputStream.readBytes())
                    cache[cacheKey] = lyrics
                    hitCompactCache(cacheKey)
                    symphony.database.lyricsCache.update(getCompactCache())
                    return lyrics
                }
        }
        val cacheKey = constructCacheKey(song.id, song.dateModified)
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
        val lrcExtReplace = Regex("""\.[^.]+${'$'}""")
        val contentUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projectedColumns = listOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA,
        )

        fun buildUri(id: Long): Uri = ContentUris.withAppendedId(contentUri, id)
        fun constructLrcPathFromSong(song: Song) = song.path.replace(lrcExtReplace, ".lrc")
        fun constructCacheKey(id: Long, dateModified: Long, prefix: String = "") =
            "$prefix-$id-$dateModified"
    }
}

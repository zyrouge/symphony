package io.github.zyrouge.symphony.services.groove

import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.concurrentSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MediaExposer(private val symphony: Symphony) {
    internal val uris = ConcurrentHashMap<String, Uri>()
    var explorer = GrooveExplorer.Folder()
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private fun emitUpdate(value: Boolean) {
        _isUpdating.update {
            value
        }
        symphony.groove.onMediaStoreUpdate(value)
    }

    private inner class ScanCycle {
        val songCache = ConcurrentHashMap(symphony.database.songCache.all())
        val songCacheUnused = concurrentSetOf(songCache.keys)
        val artworkCacheUnused = concurrentSetOf(symphony.database.artworkCache.all())
        val lyricsCacheUnused = concurrentSetOf(symphony.database.lyricsCache.keys())
    }

    fun fetch() {
        emitUpdate(true)
        try {
            val context = symphony.applicationContext
            val folderUris = symphony.settings.mediaFolders.value
            val permissions = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val cycle = ScanCycle()
            runBlocking {
                folderUris.map { x ->
                    async(Dispatchers.IO) {
                        context.contentResolver.takePersistableUriPermission(x, permissions)
                        DocumentFile.fromTreeUri(context, x)?.let {
                            scanMediaTree(cycle, it)
                        }
                    }
                }.awaitAll()
            }
            trimCache(cycle)
        } catch (err: Exception) {
            Logger.error("MediaExposer", "fetch failed", err)
        }
        emitUpdate(false)
        emitFinish()
    }

    private suspend fun scanMediaTree(cycle: ScanCycle, file: DocumentFile) {
        try {
            val filter = MediaFilter(
                symphony.settings.songsFilterPattern.value,
                symphony.settings.blacklistFolders.value.toSortedSet(),
                symphony.settings.whitelistFolders.value.toSortedSet()
            )
            val path = file.name ?: return
            if (!filter.isWhitelisted(path)) {
                return
            }
            coroutineScope {
                file.listFiles().toList().map { x ->
                    async(Dispatchers.IO) {
                        when {
                            x.isDirectory -> scanMediaTree(cycle, x)
                            x.isFile -> scanMediaFile(cycle, x)
                        }
                    }
                }.awaitAll()
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "scan media tree failed", err)
        }
    }

    private suspend fun scanMediaFile(cycle: ScanCycle, file: DocumentFile) {
        try {
            val path = file.name!!
            explorer.addRelativePath(GrooveExplorer.Path(path))
            val mimeType = file.type ?: return
            when {
                mimeType == "audio/x-mpegurl" -> scanM3UFile(cycle, file)
                path.endsWith(".lrc") -> scanLrcFile(cycle, file)
                mimeType.startsWith("audio/") -> scanAudioFile(cycle, file)
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "scan media file failed", err)
        }
    }

    private suspend fun scanAudioFile(cycle: ScanCycle, file: DocumentFile) {
        val path = file.name!!
        uris[path] = file.uri
        val lastModified = file.lastModified()
        val cached = cycle.songCache[path]?.takeIf {
            it.dateModified == lastModified
                    && it.coverFile?.let { x -> cycle.artworkCacheUnused.contains(x) } ?: true
        }
        val song = cached ?: Song.parse(symphony, file) ?: return
        if (cached == null) {
            symphony.database.songCache.put(path, song)
        }
        cycle.songCacheUnused.remove(path)
        song.coverFile?.let { cycle.artworkCacheUnused.remove(it) }
        cycle.lyricsCacheUnused.remove(song.id)
        withContext(Dispatchers.Main) {
            emitSong(song)
        }
    }

    private suspend fun scanLrcFile(cycle: ScanCycle, file: DocumentFile) {
        val path = file.name!!
        uris[path] = file.uri
    }

    private suspend fun scanM3UFile(cycle: ScanCycle, file: DocumentFile) {
        val path = file.name!!
        uris[path] = file.uri
    }

    private fun trimCache(cycle: ScanCycle) {
        try {
            symphony.database.songCache.delete(cycle.songCacheUnused)
        } catch (err: Exception) {
            Logger.warn("MediaExposer", "trim song cache failed", err)
        }
        for (x in cycle.artworkCacheUnused) {
            try {
                symphony.database.artworkCache.get(x).delete()
            } catch (err: Exception) {
                Logger.warn("MediaExposer", "delete artwork cache file failed", err)
            }
        }
        try {
            symphony.database.lyricsCache.delete(cycle.lyricsCacheUnused)
        } catch (err: Exception) {
            Logger.warn("MediaExposer", "trim lyrics cache failed", err)
        }
    }

    fun reset() {
        emitUpdate(true)
        uris.clear()
        explorer = GrooveExplorer.Folder()
        emitUpdate(false)
    }

    private fun emitSong(song: Song) {
        symphony.groove.albumArtist.onSong(song)
        symphony.groove.album.onSong(song)
        symphony.groove.artist.onSong(song)
        symphony.groove.genre.onSong(song)
        symphony.groove.song.onSong(song)
    }

    private fun emitFinish() {
        symphony.groove.albumArtist.onFinish()
        symphony.groove.album.onFinish()
        symphony.groove.artist.onFinish()
        symphony.groove.genre.onFinish()
        symphony.groove.song.onFinish()
    }

    private class MediaFilter(
        pattern: String?,
        private val blacklisted: Set<String>,
        private val whitelisted: Set<String>,
    ) {
        private val regex = pattern?.let { Regex(it, RegexOption.IGNORE_CASE) }

        fun isWhitelisted(path: String): Boolean {
            regex?.let {
                if (!it.containsMatchIn(path)) {
                    return false
                }
            }
            val bFilter = blacklisted.findLast { x -> path.startsWith(x) }
            if (bFilter == null) {
                return true
            }
            val wFilter = whitelisted.findLast { x ->
                x.startsWith(bFilter) && path.startsWith(x)
            }
            return wFilter != null
        }
    }
}

package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.ActivityUtils
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimpleFileSystem
import io.github.zyrouge.symphony.utils.SimplePath
import io.github.zyrouge.symphony.utils.concurrentSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MediaExposer(private val symphony: Symphony) {
    internal val uris = ConcurrentHashMap<String, Uri>()
    var explorer = SimpleFileSystem.Folder()
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private fun emitUpdate(value: Boolean) = _isUpdating.update {
        value
    }

    private data class ScanCycle(
        val songCache: ConcurrentHashMap<String, Song>,
        val songCacheUnused: ConcurrentSet<String>,
        val artworkCacheUnused: ConcurrentSet<String>,
        val lyricsCacheUnused: ConcurrentSet<String>,
        val filter: MediaFilter,
        val songParseOptions: Song.ParseOptions,
    ) {
        companion object {
            suspend fun create(symphony: Symphony): ScanCycle {
                val songCache = ConcurrentHashMap(symphony.database.songCache.entriesPathMapped())
                val songCacheUnused = concurrentSetOf(songCache.map { it.value.id })
                val artworkCacheUnused = concurrentSetOf(symphony.database.artworkCache.all())
                val lyricsCacheUnused = concurrentSetOf(symphony.database.lyricsCache.keys())
                val filter = MediaFilter(
                    symphony.settings.songsFilterPattern.value,
                    symphony.settings.blacklistFolders.value.toSortedSet(),
                    symphony.settings.whitelistFolders.value.toSortedSet()
                )
                return ScanCycle(
                    songCache = songCache,
                    songCacheUnused = songCacheUnused,
                    artworkCacheUnused = artworkCacheUnused,
                    lyricsCacheUnused = lyricsCacheUnused,
                    filter = filter,
                    songParseOptions = Song.ParseOptions.create(symphony),
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetch() {
        emitUpdate(true)
        try {
            val context = symphony.applicationContext
            val folderUris = symphony.settings.mediaFolders.value
            val cycle = ScanCycle.create(symphony)
            folderUris.map { x ->
                ActivityUtils.makePersistableReadableUri(context, x)
                DocumentFileX.fromTreeUri(context, x)?.let {
                    val path = SimplePath(DocumentFileX.getParentPathOfTreeUri(x) ?: it.name)
                    with(Dispatchers.IO) {
                        scanMediaTree(cycle, path, it)
                    }
                }
            }
            trimCache(cycle)
        } catch (err: Exception) {
            Logger.error("MediaExposer", "fetch failed", err)
        }
        emitUpdate(false)
        emitFinish()
    }

    private suspend fun scanMediaTree(cycle: ScanCycle, path: SimplePath, file: DocumentFileX) {
        try {
            if (!cycle.filter.isWhitelisted(path.pathString)) {
                return
            }
            coroutineScope {
                file.list().map {
                    val childPath = path.join(it.name)
                    async {
                        when {
                            it.isDirectory -> scanMediaTree(cycle, childPath, it)
                            else -> scanMediaFile(cycle, childPath, it)
                        }
                    }
                }.awaitAll()
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "scan media tree failed", err)
        }
    }

    private suspend fun scanMediaFile(cycle: ScanCycle, path: SimplePath, file: DocumentFileX) {
        try {
            when {
                path.extension == "lrc" -> scanLrcFile(cycle, path, file)
                file.mimeType == MIMETYPE_M3U -> scanM3UFile(cycle, path, file)
                file.mimeType.startsWith("audio/") -> scanAudioFile(cycle, path, file)
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "scan media file failed", err)
        }
    }

    private suspend fun scanAudioFile(cycle: ScanCycle, path: SimplePath, file: DocumentFileX) {
        val pathString = path.pathString
        uris[pathString] = file.uri
        val lastModified = file.lastModified
        val cached = cycle.songCache[pathString]
        val cacheHit = cached != null
                && cached.dateModified == lastModified
                && (cached.coverFile?.let { cycle.artworkCacheUnused.contains(it) } != false)
        val song = when {
            cacheHit -> cached
            else -> Song.parse(path, file, cycle.songParseOptions)
        }
        if (song.duration.milliseconds < symphony.settings.minSongDuration.value.seconds) {
            return
        }
        if (!cacheHit) {
            symphony.database.songCache.insert(song)
            cached?.coverFile?.let {
                if (symphony.database.artworkCache.get(it).delete()) {
                    cycle.artworkCacheUnused.remove(it)
                }
            }
        }
        cycle.songCacheUnused.remove(song.id)
        song.coverFile?.let {
            cycle.artworkCacheUnused.remove(it)
        }
        cycle.lyricsCacheUnused.remove(song.id)
        explorer.addChildFile(path)
        withContext(Dispatchers.Main) {
            emitSong(song)
        }
    }

    private fun scanLrcFile(
        @Suppress("Unused") cycle: ScanCycle,
        path: SimplePath,
        file: DocumentFileX,
    ) {
        uris[path.pathString] = file.uri
        explorer.addChildFile(path)
    }

    private fun scanM3UFile(
        @Suppress("Unused") cycle: ScanCycle,
        path: SimplePath,
        file: DocumentFileX,
    ) {
        uris[path.pathString] = file.uri
        explorer.addChildFile(path)
    }

    private suspend fun trimCache(cycle: ScanCycle) {
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

    suspend fun reset() {
        emitUpdate(true)
        uris.clear()
        explorer = SimpleFileSystem.Folder()
        symphony.database.songCache.clear()
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
        symphony.groove.playlist.onScanFinish()
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
            val bFilter = blacklisted.findLast {
                path.startsWith(it)
            }
            if (bFilter == null) {
                return true
            }
            val wFilter = whitelisted.findLast {
                it.startsWith(bFilter) && path.startsWith(it)
            }
            return wFilter != null
        }
    }

    companion object {
        const val MIMETYPE_M3U = "audio/x-mpegurl"
    }
}

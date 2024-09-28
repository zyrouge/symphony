package io.github.zyrouge.symphony.services.groove

import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    fun fetch() {
        emitUpdate(true)
        try {
            val context = symphony.applicationContext
            val folderUris = symphony.settings.mediaFolders.value
            val permissions = Intent.FLAG_GRANT_READ_URI_PERMISSION
            for (x in folderUris) {
                context.contentResolver.takePersistableUriPermission(x, permissions)
                DocumentFile.fromTreeUri(context, x)?.let {
                    scanMediaTree(it)
                }
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "fetch failed", err)
        }
        trimCache()
        emitUpdate(false)
        emitFinish()
    }

    private fun scanMediaTree(file: DocumentFile) {
        try {
            val filter = MediaFilter(
                symphony.settings.songsFilterPattern.value,
                symphony.settings.blacklistFolders.value.toSortedSet(),
                symphony.settings.whitelistFolders.value.toSortedSet()
            )
            for (x in file.listFiles()) {
                val path = file.name ?: return
                if (!filter.isWhitelisted(path)) {
                    continue
                }
                when {
                    x.isDirectory -> scanMediaTree(x)
                    x.isFile -> scanMediaFile(x)
                }
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "scan media tree failed", err)
        }
    }

    private fun scanMediaFile(file: DocumentFile) {
        try {
            val path = file.name!!
            explorer.addRelativePath(GrooveExplorer.Path(path))
            val mimeType = file.type ?: return
            when {
                mimeType == "application/x-mpegURL" -> scanM3UFile(file)
                path.endsWith(".lrc") -> scanLrcFile(file)
                mimeType.startsWith("audio/") -> scanAudioFile(file)
            }
        } catch (err: Exception) {
            Logger.error("MediaExposer", "scan media file failed", err)
        }
    }

    private fun scanAudioFile(file: DocumentFile) {
        val path = file.name!!
        uris[path] = file.uri
        val lastModified = file.lastModified()
        val cached = symphony.database.songCache.get(path)?.let {
            if (it.dateModified == lastModified) it else null
        }
        val song = cached ?: Song.parse(symphony, file) ?: return
        emitSong(song)
    }

    private fun scanLrcFile(file: DocumentFile) {
        val path = file.name!!
        uris[path] = file.uri
    }

    private fun scanM3UFile(file: DocumentFile) {
        val path = file.name!!
        uris[path] = file.uri
    }

    private fun trimCache() {
        runCatching { symphony.database.songCache.trim() }.exceptionOrNull()
            ?.let { Logger.warn("MediaExposer", "trim song cache failed", it) }
        runCatching { symphony.database.artworkCache.trim() }.exceptionOrNull()
            ?.let { Logger.warn("MediaExposer", "trim artwork cache failed", it) }
        runCatching { symphony.database.lyricsCache.trim() }.exceptionOrNull()
            ?.let { Logger.warn("MediaExposer", "trim song cache failed", it) }
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

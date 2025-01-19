package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.entities.ArtworkIndex
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeLyricsFile
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile
import io.github.zyrouge.symphony.services.groove.entities.SongFile
import io.github.zyrouge.symphony.services.groove.entities.SongLyrics
import io.github.zyrouge.symphony.utils.ActivityUtils
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.ImagePreserver
import io.github.zyrouge.symphony.utils.Logger
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
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MediaExposer(private val symphony: Symphony) {
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private fun emitUpdate(value: Boolean) = _isUpdating.update { value }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetch() {
        emitUpdate(true)
        try {
            val context = symphony.applicationContext
            val folderUris = symphony.settings.mediaFolders.value
            val scanner = Scanner.create(symphony)
            folderUris.map { x ->
                ActivityUtils.makePersistableReadableUri(context, x)
                DocumentFileX.fromTreeUri(context, x)?.let {
                    val path = SimplePath(DocumentFileX.getParentPathOfTreeUri(x) ?: it.name)
                    with(Dispatchers.IO) {
                        scanner.scanMediaTree(path, scanner.root, it)
                    }
                }
            }
            scanner.cleanup()
        } catch (err: Exception) {
            Logger.error("MediaExposer", "fetch failed", err)
        }
        emitUpdate(false)
        emitFinish()
    }

    private data class Scanner(
        val symphony: Symphony,
        val songCache: ConcurrentHashMap<String, SongFile>,
        val songStaleIds: ConcurrentSet<String>,
        val artworkIndexCache: ConcurrentHashMap<String, ArtworkIndex>,
        val artworkStaleFiles: ConcurrentSet<String>,
        val root: MediaTreeFolder,
        val filter: MediaFilter,
        val songParseOptions: SongFile.ParseOptions,
    ) {
        suspend fun scanMediaTree(path: SimplePath, parent: MediaTreeFolder, xfile: DocumentFileX) {
            try {
                if (!filter.isWhitelisted(path.pathString)) {
                    return
                }
                val exParent = symphony.database.mediaTreeFolders.findByName(parent.id, xfile.name)
                if (exParent?.dateModified == xfile.lastModified) {
                    return
                }
                val nParent = exParent ?: MediaTreeFolder(
                    id = symphony.database.mediaTreeFoldersIdGenerator.next(),
                    parentId = parent.id,
                    internalName = null,
                    name = xfile.name,
                    uri = xfile.uri,
                    dateModified = 0, // change it after scanning is done
                )
                if (exParent == null) {
                    symphony.database.mediaTreeFolders.insert(nParent)
                }
                coroutineScope {
                    xfile.list().map {
                        val nPath = path.join(it.name)
                        async {
                            when {
                                it.isDirectory -> scanMediaTree(nPath, nParent, it)
                                else -> scanMediaFile(nPath, nParent, it)
                            }
                        }
                    }.awaitAll()
                }
                symphony.database.mediaTreeFolders.update(
                    nParent.copy(dateModified = xfile.lastModified),
                )
            } catch (err: Exception) {
                Logger.error("MediaExposer", "scan media tree failed", err)
            }
        }

        suspend fun scanMediaFile(path: SimplePath, parent: MediaTreeFolder, xfile: DocumentFileX) {
            try {
                when {
                    path.extension == "lrc" -> scanLrcFile(path, parent, xfile)
                    xfile.mimeType.startsWith("audio/") -> scanAudioFile(path, parent, xfile)
                }
            } catch (err: Exception) {
                Logger.error("MediaExposer", "scan media file failed", err)
            }
        }

        suspend fun scanAudioFile(path: SimplePath, parent: MediaTreeFolder, xfile: DocumentFileX) {
            val exSongFile = songCache[path.pathString]
            val exArtworkIndex = artworkIndexCache[exSongFile?.id]
            val skipArtworkParsing = exArtworkIndex != null && exArtworkIndex.let {
                exArtworkIndex.file == null || artworkStaleFiles.contains(exArtworkIndex.file)
            }
            val skipParsing = skipArtworkParsing && exSongFile?.dateModified == xfile.lastModified
            val state: SongFileState
            val songFile: SongFile
            val artworkIndex: ArtworkIndex
            when {
                skipParsing -> {
                    state = SongFileState.Existing
                    songFile = exSongFile
                    artworkIndex = exArtworkIndex
                }

                else -> {
                    state = when {
                        exSongFile != null -> SongFileState.Updated
                        else -> SongFileState.New
                    }
                    val id = exSongFile?.id ?: symphony.database.songsIdGenerator.next()
                    val extended = SongFile.parse(id, path, xfile, songParseOptions)
                    songFile = extended.songFile
                    val artworkFile = extended.artwork?.let {
                        val extension = when (it.mimeType) {
                            "image/jpg", "image/jpeg" -> "jpg"
                            "image/png" -> "png"
                            "_" -> "_"
                            else -> null
                        }
                        if (extension == null) {
                            return@let null
                        }
                        val quality = symphony.settings.artworkQuality.value
                        if (quality.maxSide == null && extension != "_") {
                            val name = "$id.$extension"
                            symphony.database.artwork.get(name).writeBytes(it.data)
                            return@let name
                        }
                        val bitmap = BitmapFactory.decodeByteArray(it.data, 0, it.data.size)
                        val name = "$id.jpg"
                        FileOutputStream(symphony.database.artwork.get(name)).use { writer ->
                            ImagePreserver
                                .resize(bitmap, quality)
                                .compress(Bitmap.CompressFormat.JPEG, 100, writer)
                        }
                        name
                    }
                    artworkIndex = ArtworkIndex(songId = id, file = artworkFile)
                    extended.lyrics?.let {
                        symphony.database.songLyrics.upsert(SongLyrics(id, it))
                    }
                    symphony.database.songFiles.update(songFile)
                }
            }
            if (!skipArtworkParsing || !skipParsing) {
                symphony.database.artworkIndices.upsert(artworkIndex)
            }
            artworkIndex.file?.let {
                artworkStaleFiles.remove(it)
            }
            songStaleIds.remove(songFile.id)
            val exFile = symphony.database.mediaTreeSongFiles.findByName(parent.id, xfile.name)
            val file = MediaTreeSongFile(
                id = exFile?.id ?: symphony.database.mediaTreeSongFilesIdGenerator.next(),
                parentId = parent.id,
                songFileId = songFile.id,
                name = xfile.name,
                uri = xfile.uri,
                dateModified = xfile.lastModified,
            )
            when {
                exFile == null -> symphony.database.mediaTreeSongFiles.insert(file)
                else -> symphony.database.mediaTreeSongFiles.update(file)
            }
            if (songFile.duration.milliseconds < symphony.settings.minSongDuration.value.seconds) {
                return
            }
            symphony.groove.exposer.emitSongFile(state, songFile)
        }

        fun scanLrcFile(
            @Suppress("Unused") path: SimplePath,
            parent: MediaTreeFolder,
            xfile: DocumentFileX,
        ) {
            val exFile = symphony.database.mediaTreeLyricsFiles.findByName(parent.id, xfile.name)
            if (exFile?.dateModified == xfile.lastModified) {
                return
            }
            val file = MediaTreeLyricsFile(
                id = exFile?.id ?: symphony.database.mediaTreeLyricsFilesIdGenerator.next(),
                parentId = parent.id,
                name = xfile.name,
                uri = xfile.uri,
                dateModified = xfile.lastModified,
            )
            when {
                exFile == null -> symphony.database.mediaTreeLyricsFiles.insert(file)
                else -> symphony.database.mediaTreeLyricsFiles.update(file)
            }
        }

        suspend fun cleanup() {
            try {
                symphony.database.songFiles.delete(songStaleIds)
            } catch (err: Exception) {
                Logger.warn("MediaExposer", "trimming song files failed", err)
            }
            for (x in artworkStaleFiles) {
                try {
                    symphony.database.artwork.get(x).delete()
                } catch (err: Exception) {
                    Logger.warn("MediaExposer", "deleting artwork failed", err)
                }
            }
        }

        companion object {
            suspend fun create(symphony: Symphony): Scanner {
                val filter = MediaFilter(
                    symphony.settings.songsFilterPattern.value,
                    symphony.settings.blacklistFolders.value.toSortedSet(),
                    symphony.settings.whitelistFolders.value.toSortedSet()
                )
                val songEntries = symphony.database.songFiles.entriesPathMapped()
                return Scanner(
                    symphony = symphony,
                    songCache = ConcurrentHashMap(songEntries),
                    songStaleIds = concurrentSetOf(songEntries.keys),
                    artworkIndexCache = ConcurrentHashMap(symphony.database.artworkIndices.entriesSongIdMapped()),
                    artworkStaleFiles = concurrentSetOf(symphony.database.artwork.all()),
                    root = getTreeRootFolder(symphony),
                    filter = filter,
                    songParseOptions = SongFile.ParseOptions.create(symphony),
                )
            }

            private suspend fun getTreeRootFolder(symphony: Symphony): MediaTreeFolder {
                symphony.database.mediaTreeFolders.findByInternalName(MEDIA_TREE_ROOT_NAME)?.let {
                    return it
                }
                val folder = MediaTreeFolder(
                    id = symphony.database.mediaTreeFoldersIdGenerator.next(),
                    parentId = null,
                    internalName = MEDIA_TREE_ROOT_NAME,
                    name = MEDIA_TREE_ROOT_NAME,
                    uri = null,
                    dateModified = 0,
                )
                symphony.database.mediaTreeFolders.insert(folder)
                return folder
            }
        }
    }

    enum class SongFileState {
        New,
        Updated,
        Existing,
    }

    private fun emitSongFile(state: SongFileState, songFile: SongFile) {
        symphony.groove.song.onSongFile(state, songFile)
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
        const val MEDIA_TREE_ROOT_NAME = "root"
    }
}

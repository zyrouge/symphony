package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PlaylistsBox
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.services.parsers.M3UEntry
import io.github.zyrouge.symphony.utils.CursorShorty
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.getColumnIndices
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class PlaylistSortBy {
    CUSTOM,
    TITLE,
    TRACKS_COUNT,
}

class PlaylistRepository(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<String, Playlist>()
    private var favoritesId: String? = null
    private val searcher = FuzzySearcher<String>(
        options = listOf(FuzzySearchOption({ v -> get(v)?.title?.let { compareString(it) } }))
    )

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()
    private val _updateId = MutableStateFlow(0L)
    val updateId = _updateId.asStateFlow()
    private val _all = MutableStateFlow<List<String>>(emptyList())
    val all = _all.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()
    private val _favorites = MutableStateFlow<List<Long>>(emptyList())
    val favorites = _favorites.asStateFlow()

    private fun emitUpdate(value: Boolean) = _isUpdating.update {
        value
    }

    private fun emitUpdateId() = _updateId.update {
        System.currentTimeMillis()
    }

    private fun emitCount() = _count.update {
        cache.size
    }

    fun fetch() {
        emitUpdate(true)
        try {
            val data = symphony.database.playlists.read()
            val locals = queryAllLocalPlaylistsMap()
            data.custom.forEach { playlist ->
                cache[playlist.id] = playlist
                _all.update {
                    it + playlist.id
                }
                emitUpdateId()
                emitCount()
            }
            data.local.forEach {
                try {
                    locals[it.path]?.let { extended ->
                        parseLocal(extended)?.let { playlist ->
                            cache[playlist.id] = playlist
                            _all.update {
                                it + playlist.id
                            }
                            emitUpdateId()
                            emitCount()
                        }
                    }
                } catch (err: Exception) {
                    Logger.error("PlaylistRepository", "parsing ${it.path} failed", err)
                }
            }
            favoritesId = data.favorites.id
            cache[data.favorites.id] = data.favorites
            _all.update {
                it + data.favorites.id
            }
            emitUpdateId()
            emitCount()
        } catch (_: FileNotFoundException) {
        } catch (err: Exception) {
            Logger.error("PlaylistRepository", "fetch failed", err)
        }
        if (favoritesId == null) {
            createFavorites()
        }
        _favorites.update {
            getFavorites().songIds
        }
        emitUpdateId()
        emitUpdate(false)
    }

    fun reset() {
        emitUpdate(true)
        cache.clear()
        _all.update {
            emptyList()
        }
        emitCount()
        _favorites.update {
            emptyList()
        }
        emitUpdateId()
        emitUpdate(false)
    }

    fun search(playlistIds: List<String>, terms: String, limit: Int = 7) = searcher
        .search(terms, playlistIds, maxLength = limit)

    fun sort(playlistIds: List<String>, by: PlaylistSortBy, reverse: Boolean): List<String> {
        val sorted = when (by) {
            PlaylistSortBy.CUSTOM -> {
                val prefix = listOfNotNull(favoritesId)
                val others = playlistIds.toMutableList()
                prefix.forEach { others.remove(it) }
                prefix + others
            }

            PlaylistSortBy.TITLE -> playlistIds.sortedBy { get(it)?.title }
            PlaylistSortBy.TRACKS_COUNT -> playlistIds.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: String) = cache[id]
    fun get(ids: List<String>) = ids.mapNotNull { get(it) }
    fun getFavorites() = favoritesId?.let { cache[it] } ?: createFavorites()

    fun parseLocal(local: Playlist.LocalExtended) = kotlin.runCatching {
        Playlist.fromM3U(symphony, local)
    }.getOrNull()

    fun create(title: String, songIds: List<Long>) = Playlist(
        id = generatePlaylistId(),
        title = title,
        songIds = songIds,
        numberOfTracks = songIds.size,
        local = null,
    )

    suspend fun add(playlist: Playlist) {
        cache[playlist.id] = playlist
        _all.update {
            it + playlist.id
        }
        emitUpdateId()
        emitCount()
        save()
    }

    suspend fun delete(id: String) {
        cache.remove(id)
        _all.update {
            it - id
        }
        emitUpdateId()
        emitCount()
        save()
    }

    suspend fun update(id: String, songIds: List<Long>) {
        val old = get(id) ?: return
        val new = Playlist(
            id = id,
            title = old.title,
            songIds = songIds.distinct().toList(),
            numberOfTracks = songIds.size,
            local = null,
        )
        cache[id] = new
        emitUpdateId()
        emitCount()
        if (id == favoritesId) {
            _favorites.update {
                songIds
            }
        }
        save()
    }

    fun isFavorite(songId: Long): Boolean {
        val favorites = getFavorites()
        return favorites.songIds.contains(songId)
    }

    // NOTE: maybe we shouldn't use groove's coroutine scope?
    fun favorite(songId: Long) {
        val favorites = getFavorites()
        if (favorites.songIds.contains(songId)) return
        symphony.groove.coroutineScope.launch {
            update(favorites.id, favorites.songIds.mutate { add(songId) })
        }
    }

    fun unfavorite(songId: Long) {
        val favorites = getFavorites()
        if (!favorites.songIds.contains(songId)) return
        symphony.groove.coroutineScope.launch {
            update(favorites.id, favorites.songIds.mutate { remove(songId) })
        }
    }

    fun isFavoritesPlaylist(playlist: Playlist) = playlist.id == favoritesId
    fun isBuiltInPlaylist(playlist: Playlist) = isFavoritesPlaylist(playlist)

    suspend fun save() {
        val custom = mutableListOf<Playlist>()
        val local = mutableListOf<Playlist.Local>()
        val favorites = getFavorites()
        cache.values.forEach { playlist ->
            when {
                playlist.id == favorites.id -> return@forEach
                playlist.isLocal() -> playlist.local!!.let { local.add(it) }
                else -> custom.add(playlist)
            }
        }
        symphony.database.playlists.update(
            PlaylistsBox.Data(custom = custom, local = local, favorites = favorites)
        )
    }

    fun queryAllLocalPlaylists() = queryAllLocalPlaylistsMap().values.toList()
    fun queryAllLocalPlaylistsMap(): Map<String, Playlist.LocalExtended> {
        val playlists = mutableMapOf<String, Playlist.LocalExtended>()
        symphony.applicationContext.contentResolver.query(
            getExternalVolumeUri(),
            null,
            MediaStore.Files.FileColumns.MIME_TYPE + " == ?",
            arrayOf(M3U.mimeType),
            null,
        )?.use { cursor ->
            val shorty = CursorShorty(cursor, cursor.getColumnIndices(projectedColumns))
            while (cursor.moveToNext()) {
                val id = shorty.getLong(MediaStore.Files.FileColumns._ID)
                val path = shorty.getString(MediaStore.Files.FileColumns.DATA)
                if (!cache.containsKey(path)) {
                    playlists[path] = Playlist.LocalExtended(
                        id = id,
                        uri = getExternalVolumeUri(id),
                        local = Playlist.Local(path),
                    )
                }
            }
        }
        return playlists
    }

    fun savePlaylist(playlist: Playlist, uri: Uri) {
        val outputStream = symphony.applicationContext.contentResolver.openOutputStream(uri, "w")
        outputStream?.use { _ ->
            val m3u = M3U(
                playlist.songIds.mapIndexedNotNull { i, songId ->
                    symphony.groove.song.get(songId)?.let { song ->
                        val path = GrooveExplorer.Path(song.path)
                        M3UEntry(i, path.basename, song.path)
                    }
                }
            )
            outputStream.write(m3u.stringify().toByteArray())
        }
    }

    suspend fun renamePlaylist(playlist: Playlist, title: String) {
        val renamed = playlist.renamed(title)
        cache[playlist.id] = renamed
        emitUpdateId()
        save()
    }

    internal fun onMediaStoreUpdate(value: Boolean) {
        emitUpdate(value)
    }

    private fun createFavorites(): Playlist {
        val playlist = PlaylistsBox.Data.createFavoritesPlaylist()
        cache[playlist.id] = playlist
        favoritesId = playlist.id
        _all.update {
            it + playlist.id
        }
        emitUpdateId()
        emitCount()
        return playlist
    }

    companion object {
        private const val FILES_EXTERNAL_VOLUME = "external"

        val projectedColumns = listOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA
        )

        fun generatePlaylistId() = UUID.randomUUID().toString()

        private fun getExternalVolumeUri() =
            MediaStore.Files.getContentUri(FILES_EXTERNAL_VOLUME)

        private fun getExternalVolumeUri(rowId: Long) =
            MediaStore.Files.getContentUri(FILES_EXTERNAL_VOLUME, rowId)
    }
}

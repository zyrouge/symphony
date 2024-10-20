package io.github.zyrouge.symphony.services.groove

import android.content.Intent
import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.TimeBasedIncrementalKeyGenerator
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

enum class PlaylistSortBy {
    CUSTOM,
    TITLE,
    TRACKS_COUNT,
}

class PlaylistRepository(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<String, Playlist>()
    internal val idGenerator = TimeBasedIncrementalKeyGenerator()
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
    private val _favorites = MutableStateFlow<List<String>>(emptyList())
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
            val context = symphony.applicationContext
            val permissions = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val playlists = symphony.database.playlists.all()
            runBlocking {
                playlists.values.map { x ->
                    val playlist = when {
                        x.isLocal() -> {
                            context.contentResolver.takePersistableUriPermission(
                                x.uri!!,
                                permissions
                            )
                            async(Dispatchers.IO) {
                                Playlist.parse(symphony, x.uri)
                            }.await()
                        }

                        else -> x
                    }
                    cache[playlist.id] = playlist
                    _all.update {
                        it + playlist.id
                    }
                    emitUpdateId()
                    emitCount()
                }
            }
        } catch (_: FileNotFoundException) {
        } catch (err: Exception) {
            Logger.error("PlaylistRepository", "fetch failed", err)
        }
        _favorites.update {
            getFavorites().getSongIds(symphony)
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
                val prefix = listOfNotNull(FAVORITE_PLAYLIST)
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
    fun getFavorites() = cache[FAVORITE_PLAYLIST] ?: createFavorites()

    fun create(title: String, songIds: List<String>) = Playlist(
        id = idGenerator.next(),
        title = title,
        songPaths = songIds.mapNotNull { symphony.groove.song.get(it)?.path },
        uri = null,
        path = null,
    )

    fun add(playlist: Playlist) {
        cache[playlist.id] = playlist
        _all.update {
            it + playlist.id
        }
        emitUpdateId()
        emitCount()
        symphony.database.playlists.put(playlist.id, playlist)
    }

    fun delete(id: String) {
        cache.remove(id)
        _all.update {
            it - id
        }
        emitUpdateId()
        emitCount()
        symphony.database.playlists.delete(id)
    }

    fun update(id: String, songIds: List<String>) {
        val old = get(id) ?: return
        val new = Playlist(
            id = id,
            title = old.title,
            songPaths = songIds.mapNotNull { symphony.groove.song.get(it)?.path },
            uri = old.uri,
            path = old.path,
        )
        cache[id] = new
        emitUpdateId()
        emitCount()
        if (id == FAVORITE_PLAYLIST) {
            _favorites.update {
                songIds
            }
        }
        symphony.database.playlists.put(id, new)
    }

    // NOTE: maybe we shouldn't use groove's coroutine scope?
    fun favorite(songId: String) {
        val favorites = getFavorites()
        val songIds = favorites.getSongIds(symphony)
        if (songIds.contains(songId)) {
            return
        }
        symphony.groove.coroutineScope.launch {
            update(favorites.id, songIds.mutate { add(songId) })
        }
    }

    fun unfavorite(songId: String) {
        val favorites = getFavorites()
        val songIds = favorites.getSongIds(symphony)
        if (!songIds.contains(songId)) {
            return
        }
        symphony.groove.coroutineScope.launch {
            update(favorites.id, songIds.mutate { remove(songId) })
        }
    }

    fun isFavoritesPlaylist(playlist: Playlist) = playlist.id == FAVORITE_PLAYLIST
    fun isBuiltInPlaylist(playlist: Playlist) = isFavoritesPlaylist(playlist)

    fun savePlaylistToUri(playlist: Playlist, uri: Uri) {
        val outputStream = symphony.applicationContext.contentResolver.openOutputStream(uri, "w")
        outputStream?.use {
            val content = playlist.songPaths.joinToString("\n")
            it.write(content.toByteArray())
        }
    }

    fun renamePlaylist(playlist: Playlist, title: String) {
        val renamed = playlist.withTitle(title)
        cache[playlist.id] = renamed
        emitUpdateId()
        symphony.database.playlists.put(playlist.id, renamed)
    }

    private fun createFavorites(): Playlist {
        val playlist = Playlist(
            id = FAVORITE_PLAYLIST,
            title = "Favorites",
            songPaths = emptyList(),
            uri = null,
            path = null,
        )
        cache[playlist.id] = playlist
        _all.update {
            it + playlist.id
        }
        emitUpdateId()
        emitCount()
        return playlist
    }

    companion object {
        private const val FAVORITE_PLAYLIST = "favorites"
    }
}

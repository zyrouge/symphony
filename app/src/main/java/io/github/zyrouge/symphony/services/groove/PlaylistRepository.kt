package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PlaylistsBox
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class PlaylistSortBy {
    CUSTOM,
    TITLE,
    TRACKS_COUNT,
}

class PlaylistRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<String, Playlist>()
    var favoritesId: String? = null

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()
    private val _all = MutableStateFlow<List<String>>(listOf())
    val all = _all.asStateFlow()
    private val _favorites = MutableStateFlow<List<Long>>(listOf())
    val favorites = _favorites.asStateFlow()

    private fun emitUpdate(value: Boolean) = _isUpdating.tryEmit(value)
    private fun emitAll() = _all.tryEmit(cache.keys.toList())
    private fun emitFavorite(value: List<Long>) = _favorites.tryEmit(value)

    fun fetch() {
        emitUpdate(true)
        try {
            val data = symphony.database.playlists.read()
            val locals = queryAllLocalPlaylistsMap()
            data.custom.forEach { cache[it.id] = it }
            data.local.forEach {
                try {
                    locals[it.path]?.let { extended ->
                        parseLocal(extended)?.let { playlist ->
                            cache[playlist.id] = playlist
                        }
                    }
                } catch (err: Exception) {
                    Logger.error("PlaylistRepository", "parsing ${it.path} failed", err)
                }
            }
            favoritesId = data.favorites.id
            cache[data.favorites.id] = data.favorites
            emitAll()
        } catch (_: FileNotFoundException) {
        } catch (err: Exception) {
            Logger.error("PlaylistRepository", "fetch failed", err)
        }
        if (favoritesId == null) {
            createFavorites()
        }
        emitUpdate(false)
    }

    fun reset() {
        emitUpdate(true)
        cache.clear()
        emitUpdate(false)
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: String) = cache[id]
    fun getFavorites() = favoritesId?.let { cache[it] } ?: createFavorites()

    fun createFavorites(): Playlist {
        val playlist = PlaylistsBox.Data.createFavoritesPlaylist()
        cache[playlist.id] = playlist
        favoritesId = playlist.id
        return playlist
    }

    fun parseLocal(local: Playlist.LocalExtended) = kotlin.runCatching {
        Playlist.fromM3U(symphony, local)
    }.getOrNull()

    fun create(title: String, songs: List<Long>) = Playlist(
        id = generatePlaylistId(),
        title = title,
        songs = songs,
        numberOfTracks = songs.size,
        local = null,
    )

    suspend fun add(playlist: Playlist) {
        cache[playlist.id] = playlist
        emitAll()
        save()
    }

    suspend fun delete(id: String) {
        cache.remove(id)
        emitAll()
        save()
    }

    suspend fun update(id: String, songs: List<Long>) {
        val old = get(id) ?: return
        val new = Playlist(
            id = id,
            title = old.title,
            songs = songs.distinctList(),
            numberOfTracks = songs.size,
            local = null,
        )
        cache[id] = new
        emitAll()
        if (id == favoritesId) emitFavorite(songs)
        save()
    }

    fun isFavorite(song: Long): Boolean {
        val favorites = getFavorites()
        return favorites.songs.contains(song)
    }

    // NOTE: maybe we shouldn't use groove's coroutine scope?
    fun favorite(song: Long) {
        val favorites = getFavorites()
        if (favorites.songs.contains(song)) return
        symphony.groove.coroutineScope.launch {
            update(favorites.id, favorites.songs.mutate { add(song) })
        }
    }

    fun unfavorite(song: Long) {
        val favorites = getFavorites()
        if (!favorites.songs.contains(song)) return
        symphony.groove.coroutineScope.launch {
            update(favorites.id, favorites.songs.mutate { remove(song) })
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
            PlaylistsBox.Data(
                custom = custom,
                local = local,
                favorites = favorites,
            )
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
            while (cursor.moveToNext()) {
                val id = cursor.getColumnValue(MediaStore.Files.FileColumns._ID) {
                    cursor.getLong(it)
                }
                val path = cursor.getColumnValue(MediaStore.Files.FileColumns.DATA) {
                    cursor.getString(it)
                }
                if (!cache.containsKey(path)) {
                    playlists[path] = Playlist.LocalExtended(
                        id = id,
                        uri = getExternalVolumeUri(id),
                        local = Playlist.Local(path),
                    )
                }
            }
        }
        return playlists.toMap()
    }

    companion object {
        private const val FILES_EXTERNAL_VOLUME = "external"

        fun generatePlaylistId() = UUID.randomUUID().toString()

        private fun getExternalVolumeUri() =
            MediaStore.Files.getContentUri(FILES_EXTERNAL_VOLUME)

        private fun getExternalVolumeUri(rowId: Long) =
            MediaStore.Files.getContentUri(FILES_EXTERNAL_VOLUME, rowId)

        val searcher = FuzzySearcher<Playlist>(
            options = listOf(FuzzySearchOption({ it.title }))
        )

        fun search(playlists: List<Playlist>, terms: String, limit: Int? = 7) = searcher
            .search(terms, playlists)
            .subListNonStrict(limit ?: playlists.size)

        fun sort(playlists: List<Playlist>, by: PlaylistSortBy, reversed: Boolean): List<Playlist> {
            val sorted = when (by) {
                PlaylistSortBy.CUSTOM -> playlists.toList()
                PlaylistSortBy.TITLE -> playlists.sortedBy { it.title }
                PlaylistSortBy.TRACKS_COUNT -> playlists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}

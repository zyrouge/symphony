package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
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
    private val cache = ConcurrentHashMap<String, Playlist>()
    private var favoritesId: String? = null
    private val searcher = FuzzySearcher<String>(
        options = listOf(FuzzySearchOption({ get(it)?.title }))
    )

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()
    private val _all = mutableStateListOf<String>()
    private val _allRapid = RapidMutableStateList(_all)
    val all = _all.asList()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()
    private val _favorites = mutableStateListOf<Long>()
    val favorites = _favorites.asList()

    private fun emitUpdate(value: Boolean) = _isUpdating.tryEmit(value)
    private fun emitCount() = _count.tryEmit(cache.size)

    fun fetch() {
        emitUpdate(true)
        try {
            val data = symphony.database.playlists.read()
            val locals = queryAllLocalPlaylistsMap()
            data.custom.forEach { playlist ->
                cache[playlist.id] = playlist
                _allRapid.add(playlist.id)
                emitCount()
            }
            data.local.forEach {
                try {
                    locals[it.path]?.let { extended ->
                        parseLocal(extended)?.let { playlist ->
                            cache[playlist.id] = playlist
                            _allRapid.add(playlist.id)
                            emitCount()
                        }
                    }
                } catch (err: Exception) {
                    Logger.error("PlaylistRepository", "parsing ${it.path} failed", err)
                }
            }
            favoritesId = data.favorites.id
            cache[data.favorites.id] = data.favorites
            _allRapid.add(data.favorites.id)
            emitCount()
        } catch (_: FileNotFoundException) {
        } catch (err: Exception) {
            Logger.error("PlaylistRepository", "fetch failed", err)
        }
        if (favoritesId == null) {
            createFavorites()
        }
        emitUpdate(false)
        _favorites.addAll(getFavorites().songIds)
    }

    fun reset() {
        emitUpdate(true)
        cache.clear()
        _all.clear()
        emitCount()
        _favorites.clear()
        emitUpdate(false)
    }

    fun search(playlistIds: List<String>, terms: String, limit: Int? = 7) = searcher
        .search(terms, playlistIds)
        .subListNonStrict(limit ?: playlistIds.size)

    fun sort(playlistIds: List<String>, by: PlaylistSortBy, reverse: Boolean): List<String> {
        val sorted = when (by) {
            PlaylistSortBy.CUSTOM -> playlistIds
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

    fun createFavorites(): Playlist {
        val playlist = PlaylistsBox.Data.createFavoritesPlaylist()
        cache[playlist.id] = playlist
        favoritesId = playlist.id
        return playlist
    }

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
        _all.add(playlist.id)
        emitCount()
        save()
    }

    suspend fun delete(id: String) {
        cache.remove(id)
        _all.remove(id)
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
        _all.remove(id)
        _all.add(id)
        emitCount()
        if (id == favoritesId) {
            val removed = old.songIds.minus(songIds.toSet())
            val added = songIds.minus(old.songIds.toSet())
            _favorites.removeAll(removed)
            _favorites.addAll(added)
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
        return playlists
    }

    companion object {
        private const val FILES_EXTERNAL_VOLUME = "external"

        fun generatePlaylistId() = UUID.randomUUID().toString()

        private fun getExternalVolumeUri() =
            MediaStore.Files.getContentUri(FILES_EXTERNAL_VOLUME)

        private fun getExternalVolumeUri(rowId: Long) =
            MediaStore.Files.getContentUri(FILES_EXTERNAL_VOLUME, rowId)
    }
}

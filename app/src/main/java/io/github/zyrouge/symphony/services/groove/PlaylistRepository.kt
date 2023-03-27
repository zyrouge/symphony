package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PlaylistsBox
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.utils.*
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
    var isUpdating = false
    val onUpdate = Eventer.nothing()
    val onUpdateRapidDispatcher = GrooveEventerRapidUpdateDispatcher(onUpdate)
    val onFavoritesUpdate = Eventer<List<Long>>()

    private val searcher = FuzzySearcher<Playlist>(
        options = listOf(FuzzySearchOption({ it.title }))
    )

    fun fetch() {
        if (isUpdating) return
        isUpdating = true
        onUpdate.dispatch()
        try {
            val data = symphony.database.playlists.read()
            val locals = queryAllLocalPlaylistsMap()
            data.custom.forEach { cache[it.id] = it }
            data.local.forEach {
                try {
                    locals[it.path]?.let { extended ->
                        parseLocalPlaylist(extended)?.let { playlist ->
                            cache[playlist.id] = playlist
                        }
                    }
                } catch (err: Exception) {
                    Logger.error("PlaylistRepository", "parsing ${it.path} failed", err)
                }
            }
            favoritesId = data.favorites.id
            cache[data.favorites.id] = data.favorites
            onUpdateRapidDispatcher.dispatch()
        } catch (_: FileNotFoundException) {
        } catch (err: Exception) {
            Logger.error("PlaylistRepository", "fetch failed", err)
        }
        isUpdating = false
        onUpdate.dispatch()
    }

    fun reset() {
        cache.clear()
        onUpdate.dispatch()
    }

    fun getAll() = cache.values.toList()
    fun getPlaylistWithId(id: String) = cache[id]
    fun getFavoritesPlaylist() = favoritesId?.let { cache[it] }

    fun getSongsOfPlaylist(playlist: Playlist) = playlist.songs.mapNotNull {
        symphony.groove.song.getSongWithId(it)
    }

    fun getSongsOfPlaylistId(playlistId: String) = cache[playlistId]
        ?.let { getSongsOfPlaylist(it) }
        ?: listOf()

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    fun parseLocalPlaylist(local: Playlist.LocalExtended) = kotlin.runCatching {
        Playlist.fromM3U(symphony, local)
    }.getOrNull()

    fun createNewPlaylist(title: String, songs: List<Long>) = Playlist(
        id = generatePlaylistId(),
        title = title,
        songs = songs,
        numberOfTracks = songs.size,
        local = null,
    )

    suspend fun addPlaylist(playlist: Playlist) {
        cache[playlist.id] = playlist
        onUpdate.dispatch()
        save()
    }

    suspend fun removePlaylist(playlist: Playlist) = removePlaylist(playlist.id)
    suspend fun removePlaylist(id: String) {
        cache.remove(id)
        onUpdate.dispatch()
        save()
    }

    suspend fun updatePlaylistSongs(playlist: Playlist, songs: List<Long>) {
        cache[playlist.id] = Playlist(
            id = playlist.id,
            title = playlist.title,
            songs = songs.distinctList(),
            numberOfTracks = songs.size,
            local = null,
        )
        onUpdate.dispatch()
        if (playlist.id == favoritesId) {
            onFavoritesUpdate.dispatch(songs)
        }
        save()
    }

    fun isInFavorites(song: Long): Boolean {
        val favorites = getFavoritesPlaylist()
        return favorites?.songs?.contains(song) ?: false
    }

    // NOTE: maybe we shouldn't use groove's coroutine scope?
    fun addToFavorites(song: Long) {
        getFavoritesPlaylist()?.let { favorites ->
            if (favorites.songs.contains(song)) return@let
            symphony.groove.coroutineScope.launch {
                updatePlaylistSongs(
                    favorites,
                    favorites.songs.mutate { add(song) },
                )
            }
        }
    }

    fun removeFromFavorites(song: Long) {
        getFavoritesPlaylist()?.let { favorites ->
            if (!favorites.songs.contains(song)) return@let
            symphony.groove.coroutineScope.launch {
                updatePlaylistSongs(
                    favorites,
                    favorites.songs.mutate { remove(song) },
                )
            }
        }
    }

    fun isFavoritesPlaylist(playlist: Playlist) = playlist.id == favoritesId
    fun isBuiltInPlaylist(playlist: Playlist) = isFavoritesPlaylist(playlist)

    suspend fun save() {
        val custom = mutableListOf<Playlist>()
        val local = mutableListOf<Playlist.Local>()
        val favorites = getFavoritesPlaylist() ?: PlaylistsBox.Data.createFavoritesPlaylist()
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

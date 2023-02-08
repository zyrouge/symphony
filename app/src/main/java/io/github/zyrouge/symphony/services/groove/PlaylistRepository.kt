package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PlaylistsBox
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.utils.*
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class PlaylistSortBy {
    CUSTOM,
    TITLE,
    TRACKS_COUNT,
}

class PlaylistRepository(private val symphony: Symphony) {
    private val cached = ConcurrentHashMap<String, Playlist>()
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<Playlist>(
        options = listOf(FuzzySearchOption({ it.title }))
    )

    fun fetch() {
        if (isUpdating) return
        isUpdating = true
        onUpdate.dispatch(null)
        try {
            val data = symphony.database.playlists.read()
            val locals = queryAllLocalPlaylistsMap()
            data.custom.forEach { cached[it.id] = it }
            data.local.forEach {
                try {
                    locals[it.path]?.let { extended ->
                        parseLocalPlaylist(extended)?.let { playlist ->
                            cached[playlist.id] = playlist
                        }
                    }
                } catch (err: Exception) {
                    Logger.error("PlaylistRepository", "parsing ${it.path} failed: $err")
                }
            }
        } catch (_: FileNotFoundException) {
        } catch (err: Exception) {
            Logger.error("PlaylistRepository", "fetch failed: $err")
        }
        isUpdating = false
        onUpdate.dispatch(null)
    }

    fun reset() {
        cached.clear()
        onUpdate.dispatch(null)
    }

    fun getAll() = cached.values.toList()
    fun getPlaylistWithId(id: String) = cached[id]

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    fun parseLocalPlaylist(local: Playlist.LocalExtended) = kotlin.runCatching {
        Playlist.fromM3U(symphony, local)
    }.getOrNull()

    fun createNewPlaylist(title: String, songs: List<Long>) = Playlist(
        id = UUID.randomUUID().toString(),
        title = title,
        songs = songs,
        numberOfTracks = songs.size,
        local = null,
    )

    suspend fun addPlaylist(playlist: Playlist) {
        cached[playlist.id] = playlist
        onUpdate.dispatch(null)
        save()
    }

    suspend fun removePlaylist(playlist: Playlist) = removePlaylist(playlist.id)
    suspend fun removePlaylist(id: String) {
        cached.remove(id)
        onUpdate.dispatch(null)
        save()
    }

    suspend fun updatePlaylistSongs(playlist: Playlist, songs: List<Long>) {
        cached[playlist.id] = Playlist(
            id = playlist.id,
            title = playlist.title,
            songs = songs,
            numberOfTracks = songs.size,
            local = null,
        )
        onUpdate.dispatch(null)
        save()
    }

    suspend fun save() {
        val custom = mutableListOf<Playlist>()
        val local = mutableListOf<Playlist.Local>()
        cached.values.forEach { playlist ->
            if (playlist.isLocal()) playlist.local!!.let { local.add(it) }
            else custom.add(playlist)
        }
        symphony.database.playlists.update(PlaylistsBox.Data(custom = custom, local = local))
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
                if (!cached.containsKey(path)) {
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

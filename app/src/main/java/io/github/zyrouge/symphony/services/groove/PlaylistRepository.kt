package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PlaylistsBox
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.getColumnValue
import java.io.FileNotFoundException
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

    fun fetch() {
        if (isUpdating) return
        isUpdating = true
        try {
            val data = symphony.database.playlists.read()
            data.custom.forEach { cached[it.id] = it }
            data.local.forEach {
                try {
                    val playlist = Playlist.fromM3U(symphony, it)
                    cached[playlist.id] = playlist
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

    fun getAll() = cached.values.toList()
    fun getPlaylistWithId(id: String) = cached[id]

    suspend fun addLocalPlaylist(local: Playlist.Local) {
        val playlist = Playlist.fromM3U(symphony, local)
        cached[playlist.id] = playlist
        onUpdate.dispatch(null)
        save()
    }

    suspend fun removePlaylist(id: String) {
        cached.remove(id)
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

    fun queryAllLocalPlaylists(): List<Playlist.Local> {
        val playlists = mutableListOf<Playlist.Local>()
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
                    playlists.add(
                        Playlist.Local(
                            id = id,
                            path = path,
                            uri = getExternalVolumeUri(id),
                        )
                    )
                }
            }
        }
        return playlists.toList()
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

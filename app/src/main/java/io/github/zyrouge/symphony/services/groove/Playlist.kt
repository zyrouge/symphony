package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.utils.toList
import org.json.JSONArray
import org.json.JSONObject

@Immutable
data class Playlist(
    val id: String,
    val title: String,
    val songIds: List<Long>,
    val numberOfTracks: Int,
    val local: Local?,
) {
    val basename: String get() = "$title.m3u"

    data class LocalExtended(val id: Long, val uri: Uri, val local: Local) {
        val path: String get() = local.path
    }

    @Immutable
    data class Local(val path: String) {
        fun toJSONObject(): JSONObject {
            val json = JSONObject()
            json.put(PLAYLIST_LOCAL_PATH_KEY, path)
            return json
        }

        companion object {
            const val PLAYLIST_LOCAL_PATH_KEY = "l_path"

            fun fromJSONObject(serialized: JSONObject): Local {
                return Local(serialized.getString(PLAYLIST_LOCAL_PATH_KEY))
            }
        }
    }

    fun createArtworkImageRequest(symphony: Symphony) =
        songIds.firstOrNull()
            ?.let { symphony.groove.song.get(it)?.createArtworkImageRequest(symphony) }
            ?: Assets.createPlaceholderImageRequest(symphony)

    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        songIds,
        symphony.settings.getLastUsedPlaylistSongsSortBy(),
        symphony.settings.getLastUsedPlaylistSongsSortReverse(),
    )

    fun isLocal() = local != null
    fun isNotLocal() = local == null

    fun renamed(title: String) = Playlist(
        id = id,
        title = title,
        songIds = songIds,
        numberOfTracks = numberOfTracks,
        local = local,
    )

    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        json.put(PLAYLIST_ID_KEY, id)
        json.put(PLAYLIST_TITLE_KEY, title)
        json.put(PLAYLIST_SONGS_KEY, JSONArray(songIds))
        json.put(PLAYLIST_NUMBER_OF_TRACKS_KEY, numberOfTracks)
        return json
    }

    companion object {
        private const val ROOT_STORAGE = "/storage/emulated/0"

        const val PLAYLIST_ID_KEY = "id"
        const val PLAYLIST_TITLE_KEY = "title"
        const val PLAYLIST_SONGS_KEY = "songs"
        const val PLAYLIST_NUMBER_OF_TRACKS_KEY = "n_tracks"

        fun fromJSONObject(serialized: JSONObject) = Playlist(
            id = serialized.getString(PLAYLIST_ID_KEY),
            title = serialized.getString(PLAYLIST_TITLE_KEY),
            songIds = serialized.getJSONArray(PLAYLIST_SONGS_KEY).toList { getLong(it) },
            numberOfTracks = serialized.getInt(PLAYLIST_NUMBER_OF_TRACKS_KEY),
            local = null,
        )

        fun fromM3U(symphony: Symphony, local: LocalExtended): Playlist {
            val path = GrooveExplorer.Path(local.path)
            val dir = path.dirname
            val content = symphony.applicationContext.contentResolver
                .openInputStream(local.uri)
                ?.use { String(it.readBytes()) } ?: ""
            val m3u = M3U.parse(content)
            val songs = mutableListOf<Long>()
            m3u.entries.forEach { entry ->
                val resolvedPath = when {
                    symphony.groove.song.pathCache.containsKey(entry.path) -> entry.path
                    GrooveExplorer.Path.isAbsolute(entry.path) -> ROOT_STORAGE + entry.path
                    else -> "/" + dir.resolve(GrooveExplorer.Path(entry.path)).toString()
                }
                val id = symphony.groove.song.pathCache[resolvedPath]
                id?.let { songs.add(it) }
            }
            return Playlist(
                id = local.path,
                title = path.basename.removeSuffix(".m3u"),
                songIds = songs,
                numberOfTracks = songs.size,
                local = local.local,
            )
        }
    }
}

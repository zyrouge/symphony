package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import coil.request.ImageRequest
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import org.json.JSONArray
import org.json.JSONObject

@Immutable
data class Playlist(
    val id: String,
    val title: String,
    val songs: List<Long>,
    val numberOfTracks: Int,
    val path: String?,
) {
    fun createArtworkImageRequest(symphony: Symphony) =
        songs.firstOrNull()
            ?.let { symphony.groove.song.getSongWithId(it)?.createArtworkImageRequest(symphony) }
            ?: ImageRequest.Builder(symphony.applicationContext)
                .data(Assets.getPlaceholderUri(symphony.applicationContext))

    fun stringify(): String {
        val json = JSONObject()
        json.put(PLAYLIST_ID_KEY, id)
        json.put(PLAYLIST_TITLE_KEY, title)
        json.put(PLAYLIST_SONGS_KEY, JSONArray(songs))
        json.put(PLAYLIST_NUMBER_OF_TRACKS_KEY, numberOfTracks)
        return json.toString()
    }

    companion object {
        const val PLAYLIST_ID_KEY = "id"
        const val PLAYLIST_TITLE_KEY = "title"
        const val PLAYLIST_SONGS_KEY = "songs"
        const val PLAYLIST_NUMBER_OF_TRACKS_KEY = "n_tracks"

        fun parse(serialized: JSONObject, path: String?): Playlist {
            val songs = serialized.getJSONArray(PLAYLIST_SONGS_KEY).let {
                MutableList(it.length()) { i -> it.getLong(i) }.toList()
            }
            return Playlist(
                id = serialized.getString(PLAYLIST_ID_KEY),
                title = serialized.getString(PLAYLIST_TITLE_KEY),
                songs = songs,
                numberOfTracks = serialized.getInt(PLAYLIST_NUMBER_OF_TRACKS_KEY),
                path = path,
            )
        }
    }
}

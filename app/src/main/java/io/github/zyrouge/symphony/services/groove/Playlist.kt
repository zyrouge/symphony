package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.documentfile.provider.DocumentFile
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.utils.RelaxedJsonDecoder
import io.github.zyrouge.symphony.utils.UriSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

@Immutable
@Serializable
data class Playlist(
    @SerialName(KEY_ID)
    val id: String,
    @SerialName(KEY_TITLE)
    val title: String,
    @SerialName(KEY_SONG_PATHS)
    val songPaths: List<String>,
    @SerialName(KEY_URI)
    @Serializable(UriSerializer::class)
    val uri: Uri?,
    @SerialName(KEY_PATH)
    val path: String?,
) {
    val numberOfTracks: Int get() = songPaths.size
    val basename: String get() = path?.let { Path(it).name } ?: "$title.m3u"
    private val dirname: String? get() = path?.let { Path(it).parent.pathString }

    fun createArtworkImageRequest(symphony: Symphony) =
        getSongIds(symphony).firstOrNull()
            ?.let { symphony.groove.song.get(it)?.createArtworkImageRequest(symphony) }
            ?: Assets.createPlaceholderImageRequest(symphony)

    fun getSongIds(symphony: Symphony) = songPaths.mapNotNull { x ->
        symphony.groove.song.pathCache[x]
            ?: dirname?.let { symphony.groove.song.pathCache[Path(it, x).pathString] }
            ?: symphony.groove.song.pathCache[Path(ROOT_STORAGE, x).pathString]
    }

    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.lastUsedPlaylistSongsSortBy.value,
        symphony.settings.lastUsedPlaylistSongsSortReverse.value,
    )

    fun isLocal() = uri != null
    fun isNotLocal() = uri == null

    fun withTitle(title: String) = Playlist(
        id = id,
        title = title,
        songPaths = songPaths,
        uri = uri,
        path = path,
    )

    fun toJson() = Json.encodeToString(this)

    companion object {
        private const val ROOT_STORAGE = "/storage/emulated/0"

        const val KEY_ID = "0"
        const val KEY_TITLE = "1"
        const val KEY_SONG_PATHS = "2"
        const val KEY_URI = "3"
        const val KEY_PATH = "4"

        fun fromJson(json: String) = RelaxedJsonDecoder.decodeFromString<Playlist>(json)

        fun parse(symphony: Symphony, uri: Uri): Playlist {
            val file = DocumentFile.fromSingleUri(symphony.applicationContext, uri)!!
            val path = file.name!!
            val content = symphony.applicationContext.contentResolver.openInputStream(uri)
                ?.use { String(it.readBytes()) } ?: ""
            val songPaths = content.lineSequence()
                .map { it.trim() }
                .filter {
                    it.isNotEmpty() && it[0] != '#'
                }
                .toList()
            val id = symphony.groove.playlist.idGenerator.next()
            return Playlist(
                id = id,
                title = Path(path).nameWithoutExtension,
                songPaths = songPaths,
                uri = uri,
                path = path,
            )
        }
    }
}

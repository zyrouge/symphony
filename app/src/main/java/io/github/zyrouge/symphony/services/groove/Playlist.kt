package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.SimplePath
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

@Immutable
@Entity("playlists")
data class Playlist(
    @PrimaryKey
    val id: String,
    val title: String,
    val songPaths: List<String>,
    val uri: Uri?,
    val path: String?,
) {
    val numberOfTracks: Int get() = songPaths.size
    val isLocal get() = uri != null
    val isNotLocal get() = uri == null

    fun createArtworkImageRequest(symphony: Symphony) =
        getSongIds(symphony).firstOrNull()
            ?.let { symphony.groove.song.get(it)?.createArtworkImageRequest(symphony) }
            ?: Assets.createPlaceholderImageRequest(symphony)

    fun getSongIds(symphony: Symphony): List<String> {
        val parentPath = path?.let { SimplePath(it) }?.parent
        val primaryPath = SimplePath(PRIMARY_STORAGE)
        return songPaths.mapNotNull { x ->
            symphony.groove.song.pathCache[x]
                ?: x.takeIf { x[0] == '/' }?.let {
                    symphony.groove.song.pathCache[it.substring(1).replaceFirst("/", ":")]
                }
                ?: parentPath?.let { symphony.groove.song.pathCache[it.join(x).pathString] }
                ?: symphony.groove.song.pathCache[primaryPath.join(x).pathString]
        }
    }

    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.lastUsedPlaylistSongsSortBy.value,
        symphony.settings.lastUsedPlaylistSongsSortReverse.value,
    )

    fun withTitle(title: String) = Playlist(
        id = id,
        title = title,
        songPaths = songPaths,
        uri = uri,
        path = path,
    )

    companion object {
        private const val PRIMARY_STORAGE = "primary:"

        fun parse(symphony: Symphony, uri: Uri): Playlist {
            val file = DocumentFileX.fromSingleUri(symphony.applicationContext, uri)!!
            val content = symphony.applicationContext.contentResolver.openInputStream(uri)
                ?.use { String(it.readBytes()) } ?: ""
            val songPaths = content.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && it[0] != '#' }
                .toList()
            val id = symphony.groove.playlist.idGenerator.next()
            val path = DocumentFileX.getParentPathOfSingleUri(file.uri) ?: file.name
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

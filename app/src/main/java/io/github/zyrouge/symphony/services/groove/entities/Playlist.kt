package io.github.zyrouge.symphony.services.groove.entities

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.SimplePath
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

@Immutable
@Entity(
    Playlist.TABLE,
    indices = [Index(Playlist.COLUMN_TITLE)],
)
data class Playlist(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_TITLE)
    val title: String,
    @ColumnInfo(COLUMN_URI)
    val uri: Uri?,
    @ColumnInfo(COLUMN_PATH)
    val path: String?,
) {
    data class Extended(val playlist: Playlist, val songPaths: List<String>)

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

    companion object {
        const val TABLE = "playlists"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_URI = "title"
        const val COLUMN_PATH = "path"

        private const val PRIMARY_STORAGE = "primary:"

        fun parse(symphony: Symphony, playlistId: String?, uri: Uri): Extended {
            val file = DocumentFileX.fromSingleUri(symphony.applicationContext, uri)!!
            val content = symphony.applicationContext.contentResolver.openInputStream(uri)
                ?.use { String(it.readBytes()) } ?: ""
            val songPaths = content.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && it[0] != '#' }
                .toList()
            val id = playlistId ?: symphony.groove.playlist.idGenerator.next()
            val path = DocumentFileX.getParentPathOfSingleUri(file.uri) ?: file.name
            val playlist = Playlist(
                id = id,
                title = Path(path).nameWithoutExtension,
                uri = uri,
                path = path,
            )
            return Extended(playlist = playlist, songPaths = songPaths)
        }
    }
}

package io.github.zyrouge.symphony.services.groove.entities

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.DocumentFileX
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
    data class AlongAttributes(
        @Embedded
        val playlist: Playlist,
        @Embedded
        val tracksCount: Int,
    ) {
        companion object {
            const val EMBEDDED_TRACKS_COUNT = "tracksCount"
        }
    }

    data class Parsed(val playlist: Playlist, val songPaths: List<String>)

    companion object {
        const val TABLE = "playlists"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_URI = "title"
        const val COLUMN_PATH = "path"

        private const val PRIMARY_STORAGE = "primary:"
        const val MIMETYPE_M3U = ""

        fun parse(symphony: Symphony, id: String, uri: Uri): Parsed {
            val file = DocumentFileX.fromSingleUri(symphony.applicationContext, uri)!!
            val content = symphony.applicationContext.contentResolver.openInputStream(uri)
                ?.use { String(it.readBytes()) } ?: ""
            val songPaths = content.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && it[0] != '#' }
                .toList()
            val path = DocumentFileX.getParentPathOfSingleUri(file.uri) ?: file.name
            val playlist = Playlist(
                id = id,
                title = Path(path).nameWithoutExtension,
                uri = uri,
                path = path,
            )
            return Parsed(playlist = playlist, songPaths = songPaths)
        }
    }
}

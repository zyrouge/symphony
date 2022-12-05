package io.github.zyrouge.symphony.services.groove

import android.database.Cursor
import android.provider.MediaStore.Audio.AlbumColumns
import androidx.compose.runtime.Immutable
import androidx.core.database.getStringOrNull
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.getColumnValue
import io.github.zyrouge.symphony.utils.getColumnValueNullable

@Immutable
data class Album(
    val albumId: Long,
    val albumName: String,
    val artistName: String?,
    val numberOfTracks: Int,
) {
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.album.createAlbumArtworkImageRequest(albumId)

    companion object {
        fun fromCursor(cursor: Cursor): Album {
            return Album(
                albumId = cursor.getColumnValue(AlbumColumns.ALBUM_ID) {
                    cursor.getLong(it)
                },
                albumName = cursor.getColumnValue(AlbumColumns.ALBUM) {
                    cursor.getString(it)
                },
                artistName = cursor.getColumnValueNullable(AlbumColumns.NUMBER_OF_SONGS) {
                    cursor.getStringOrNull(it)
                },
                numberOfTracks = cursor.getColumnValue(AlbumColumns.NUMBER_OF_SONGS) {
                    cursor.getInt(it)
                },
            )
        }
    }
}

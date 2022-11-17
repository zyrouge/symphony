package io.github.zyrouge.symphony.services.groove

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import androidx.compose.runtime.Immutable
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.getColumnValue
import io.github.zyrouge.symphony.utils.getColumnValueNullable
import kotlin.io.path.Path

@Immutable
data class Song(
    val id: Long,
    val title: String,
    val trackNumber: Int?,
    val year: Int?,
    val duration: Long,
    val albumId: Long,
    val albumName: String?,
    val artistId: Long,
    val artistName: String?,
    val composer: String?,
    val albumArtist: String?,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val path: String,
    val genre: String?,
    val bitrate: Int?,
) {
    val filename = Path(path).fileName.toString()

    val uri: Uri
        get() = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())

    fun getArtworkUri(symphony: Symphony) = symphony.groove.album.getAlbumArtworkUri(albumId)

    companion object {
        fun fromCursor(cursor: Cursor): Song {
            return Song(
                id = cursor.getColumnValue(AudioColumns._ID) {
                    cursor.getLong(it)
                },
                title = cursor.getColumnValue(AudioColumns.TITLE) {
                    cursor.getString(it)
                },
                trackNumber = cursor.getColumnValueNullable(AudioColumns.TRACK) {
                    cursor.getIntOrNull(it)
                },
                year = cursor.getColumnValueNullable(AudioColumns.YEAR) {
                    cursor.getIntOrNull(it)
                },
                duration = cursor.getColumnValue(AudioColumns.DURATION) {
                    cursor.getLong(it)
                },
                albumId = cursor.getColumnValue(AudioColumns.ALBUM_ID) {
                    cursor.getLong(it)
                },
                albumName = cursor.getColumnValueNullable(AudioColumns.ALBUM) {
                    cursor.getStringOrNull(it)
                },
                artistId = cursor.getColumnValue(AudioColumns.ARTIST_ID) {
                    cursor.getLong(it)
                },
                artistName = cursor.getColumnValueNullable(AudioColumns.ARTIST) {
                    cursor.getStringOrNull(it)
                },
                composer = cursor.getColumnValueNullable(AudioColumns.COMPOSER) {
                    cursor.getStringOrNull(it)
                },
                albumArtist = cursor.getColumnValueNullable(AudioColumns.ALBUM_ARTIST) {
                    cursor.getStringOrNull(it)
                },
                dateAdded = cursor.getColumnValue(AudioColumns.DATE_ADDED) {
                    cursor.getLong(it)
                },
                dateModified = cursor.getColumnValue(AudioColumns.DATE_MODIFIED) {
                    cursor.getLong(it)
                },
                size = cursor.getColumnValue(AudioColumns.SIZE) {
                    cursor.getLong(it)
                },
                path = cursor.getColumnValue(AudioColumns.DATA) {
                    cursor.getString(it)
                },
                genre = cursor.getColumnValueNullable(AudioColumns.GENRE) {
                    cursor.getString(it)
                },
                bitrate = cursor.getColumnValueNullable(AudioColumns.BITRATE) {
                    cursor.getInt(it)
                },
            )
        }
    }
}

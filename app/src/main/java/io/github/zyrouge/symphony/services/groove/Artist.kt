package io.github.zyrouge.symphony.services.groove

import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore.Audio.ArtistColumns
import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.getColumnValue

@Immutable
data class Artist(
    val artistName: String,
    val numberOfAlbums: Int,
    val numberOfTracks: Int
) {
    fun getArtwork(symphony: Symphony, size: Int): Bitmap {
        return symphony.groove.artist.fetchArtistArtwork(artistName, size)
    }

    companion object {
        fun fromCursor(cursor: Cursor): Artist {
            return Artist(
                artistName = cursor.getColumnValue(ArtistColumns.ARTIST) {
                    cursor.getString(it)
                },
                numberOfAlbums = cursor.getColumnValue(ArtistColumns.NUMBER_OF_ALBUMS) {
                    cursor.getInt(it)
                },
                numberOfTracks = cursor.getColumnValue(ArtistColumns.NUMBER_OF_TRACKS) {
                    cursor.getInt(it)
                }
            )
        }
    }
}

package io.github.zyrouge.symphony.services.groove

import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore.Audio.ArtistColumns
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.getColumnValue

data class Artist(
//    val artistId: Long,
    val artistName: String,
    val numberOfAlbums: Int,
    val numberOfTracks: Int
) {
    fun getArtwork(): Bitmap {
        return Symphony.groove.artist.fetchArtistArtwork(artistName)
    }

    companion object {
        fun fromCursor(cursor: Cursor): Artist {
            return Artist(
//                artistId = cursor.getColumnValue(AudioColumns.ARTIST_ID) {
//                    cursor.getLong(it)
//                },
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

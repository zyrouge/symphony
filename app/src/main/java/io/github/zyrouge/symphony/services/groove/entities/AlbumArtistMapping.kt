package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    AlbumArtistMapping.TABLE,
    primaryKeys = [AlbumArtistMapping.COLUMN_ALBUM_ID, AlbumArtistMapping.COLUMN_ARTIST_ID],
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = arrayOf(Album.COLUMN_ID),
            childColumns = arrayOf(AlbumArtistMapping.COLUMN_ARTIST_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(AlbumArtistMapping.COLUMN_ARTIST_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(AlbumArtistMapping.COLUMN_ALBUM_ID),
        Index(AlbumArtistMapping.COLUMN_ARTIST_ID),
        Index(AlbumArtistMapping.COLUMN_IS_ALBUM_ARTIST),
    ],
)
data class AlbumArtistMapping(
    @ColumnInfo(COLUMN_ALBUM_ID)
    val albumId: String,
    @ColumnInfo(COLUMN_ARTIST_ID)
    val artistId: String,
    @ColumnInfo(COLUMN_IS_ALBUM_ARTIST)
    val isAlbumArtist: Boolean,
) {
    companion object {
        const val TABLE = "album_artists_mapping"
        const val COLUMN_ALBUM_ID = "album_id"
        const val COLUMN_ARTIST_ID = "artist_id"
        const val COLUMN_IS_ALBUM_ARTIST = "is_album_artist"
    }
}

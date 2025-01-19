package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    AlbumSongMapping.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = arrayOf(Album.COLUMN_ID),
            childColumns = arrayOf(AlbumSongMapping.COLUMN_ALBUM_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(AlbumSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(AlbumSongMapping.COLUMN_ALBUM_ID)],
)
data class AlbumSongMapping(
    @ColumnInfo(COLUMN_ALBUM_ID)
    val albumId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String,
) {
    companion object {
        const val TABLE = "album_songs_mapping"
        const val COLUMN_ALBUM_ID = "album_id"
        const val COLUMN_SONG_ID = "song_id"
    }
}

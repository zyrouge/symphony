package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    GenreSongMapping.TABLE,
    primaryKeys = [GenreSongMapping.COLUMN_GENRE_ID, GenreSongMapping.COLUMN_SONG_ID],
    foreignKeys = [
        ForeignKey(
            entity = AlbumArtistMapping::class,
            parentColumns = arrayOf(Genre.COLUMN_ID),
            childColumns = arrayOf(GenreSongMapping.COLUMN_GENRE_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(GenreSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(GenreSongMapping.COLUMN_GENRE_ID),
        Index(GenreSongMapping.COLUMN_SONG_ID),
    ],
)
data class GenreSongMapping(
    @ColumnInfo(COLUMN_GENRE_ID)
    val genreId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String,
) {
    companion object {
        const val TABLE = "genre_songs_mapping"
        const val COLUMN_GENRE_ID = "genre_id"
        const val COLUMN_SONG_ID = "song_id"
    }
}

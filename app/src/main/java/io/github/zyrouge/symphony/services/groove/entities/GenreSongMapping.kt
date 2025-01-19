package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    GenreSongMapping.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = AlbumArtistMapping::class,
            parentColumns = arrayOf(Genre.COLUMN_ID),
            childColumns = arrayOf(GenreSongMapping.GENRE_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(GenreSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(GenreSongMapping.GENRE_ID)],
)
data class GenreSongMapping(
    @ColumnInfo(GENRE_ID)
    val genreId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String,
) {
    companion object {
        const val TABLE = "artist_songs_mapping"
        const val GENRE_ID = "genre_id"
        const val COLUMN_SONG_ID = "song_id"
    }
}

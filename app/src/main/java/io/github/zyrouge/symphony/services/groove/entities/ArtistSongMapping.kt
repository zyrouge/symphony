package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    ArtistSongMapping.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = AlbumArtistMapping::class,
            parentColumns = arrayOf(Artist.COLUMN_ID),
            childColumns = arrayOf(ArtistSongMapping.COLUMN_ARTIST_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(ArtistSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(ArtistSongMapping.COLUMN_ARTIST_ID)],
)
data class ArtistSongMapping(
    @ColumnInfo(COLUMN_ARTIST_ID)
    val artistId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String,
) {
    companion object {
        const val TABLE = "artist_songs_mapping"
        const val COLUMN_ARTIST_ID = "artist_id"
        const val COLUMN_SONG_ID = "song_id"
    }
}

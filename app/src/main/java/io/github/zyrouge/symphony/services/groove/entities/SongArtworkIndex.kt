package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    SongArtworkIndex.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(SongArtworkIndex.COLUMN_SONG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(SongArtworkIndex.COLUMN_FILE)],
)
data class SongArtworkIndex(
    @PrimaryKey
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String,
    @ColumnInfo(COLUMN_FILE)
    val file: String?,
) {
    companion object {
        const val TABLE = "song_artwork_indices"
        const val COLUMN_SONG_ID = "song_id"
        const val COLUMN_FILE = "file"
    }
}

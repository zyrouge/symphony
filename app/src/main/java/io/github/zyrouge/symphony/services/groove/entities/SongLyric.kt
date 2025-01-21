package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Immutable
@Entity(
    SongLyric.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(SongLyric.COLUMN_SONG_FILE_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SongLyric(
    @PrimaryKey
    @ColumnInfo(COLUMN_SONG_FILE_ID)
    val songFileId: String,
    @ColumnInfo(COLUMN_LYRICS)
    val lyrics: String,
) {
    companion object {
        const val TABLE = "song_lyrics"
        const val COLUMN_SONG_FILE_ID = "song_file_id"
        const val COLUMN_LYRICS = "lyrics"
    }
}

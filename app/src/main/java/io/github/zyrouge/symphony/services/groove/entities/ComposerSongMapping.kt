package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    ComposerSongMapping.TABLE,
    primaryKeys = [ComposerSongMapping.COLUMN_COMPOSER_ID, ComposerSongMapping.COLUMN_SONG_ID],
    foreignKeys = [
        ForeignKey(
            entity = Composer::class,
            parentColumns = arrayOf(Composer.COLUMN_ID),
            childColumns = arrayOf(ComposerSongMapping.COLUMN_COMPOSER_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(ComposerSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(ComposerSongMapping.COLUMN_COMPOSER_ID),
        Index(ComposerSongMapping.COLUMN_SONG_ID),
    ],
)
data class ComposerSongMapping(
    @ColumnInfo(COLUMN_COMPOSER_ID)
    val composerId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String,
) {
    companion object {
        const val TABLE = "composer_songs_mapping"
        const val COLUMN_COMPOSER_ID = "composer_id"
        const val COLUMN_SONG_ID = "song_id"
    }
}

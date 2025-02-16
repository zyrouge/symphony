package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    Album.TABLE,
    indices = [Index(Album.COLUMN_NAME)],
)
data class Album(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_NAME)
    val name: String,
    @ColumnInfo(COLUMN_START_YEAR)
    val startYear: Int?,
    @ColumnInfo(COLUMN_END_YEAR)
    val endYear: Int?,
) {
    data class AlongAttributes(
        @Embedded
        val entity: Album,
        @Embedded
        val tracksCount: Int,
        @Embedded
        val artistsCount: Int,
    ) {
        companion object {
            const val EMBEDDED_TRACKS_COUNT = "tracksCount"
            const val EMBEDDED_ARTISTS_COUNT = "artistsCount"
        }
    }

    companion object {
        const val TABLE = "albums"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_START_YEAR = "start_year"
        const val COLUMN_END_YEAR = "end_year"
    }
}

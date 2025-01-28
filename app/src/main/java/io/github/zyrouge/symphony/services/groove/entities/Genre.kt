package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    Genre.TABLE,
    indices = [Index(Genre.COLUMN_NAME)],
)
data class Genre(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_NAME)
    val name: String,
) {
    data class AlongAttributes(
        @Embedded
        val genre: Genre,
        @Embedded
        val tracksCount: Int,
    ) {
        companion object {
            const val EMBEDDED_TRACKS_COUNT = "tracksCount"
        }
    }

    companion object {
        const val TABLE = "genres"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }
}

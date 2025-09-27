package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    Artist.TABLE,
    indices = [Index(Artist.COLUMN_NAME)],
)
data class Artist(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_NAME)
    val name: String,
) {
    data class AlongAttributes(
        @Embedded
        val entity: Artist,
        val tracksCount: Int,
        val albumsCount: Int,
    ) {
        companion object {
            const val EMBEDDED_TRACKS_COUNT = "tracksCount"
            const val EMBEDDED_ALBUMS_COUNT = "albumsCount"
        }
    }

    companion object {
        const val TABLE = "artists"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }
}

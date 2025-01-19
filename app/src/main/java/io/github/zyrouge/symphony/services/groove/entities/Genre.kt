package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony

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
    fun getSongIds(symphony: Symphony) = symphony.groove.genre.getSongIds(name)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.lastUsedSongsSortBy.value,
        symphony.settings.lastUsedSongsSortReverse.value,
    )

    companion object {
        const val TABLE = "genres"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }
}

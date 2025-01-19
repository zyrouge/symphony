package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony

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
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.album.createArtworkImageRequest(id)

    fun getSongIds(symphony: Symphony) = symphony.groove.album.getSongIds(id)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.lastUsedAlbumSongsSortBy.value,
        symphony.settings.lastUsedAlbumSongsSortReverse.value,
    )

    companion object {
        const val TABLE = "albums"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_START_YEAR = "start_year"
        const val COLUMN_END_YEAR = "end_year"
    }
}

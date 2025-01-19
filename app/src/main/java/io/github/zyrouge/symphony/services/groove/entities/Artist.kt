package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony

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
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.artist.createArtworkImageRequest(name)

    fun getSongIds(symphony: Symphony) = symphony.groove.artist.getSongIds(name)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.lastUsedSongsSortBy.value,
        symphony.settings.lastUsedSongsSortReverse.value,
    )

    fun getAlbumIds(symphony: Symphony) = symphony.groove.artist.getAlbumIds(name)

    companion object {
        const val TABLE = "artists"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }
}

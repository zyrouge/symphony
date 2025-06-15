package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    PlaylistSongMapping.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = arrayOf(Playlist.COLUMN_ID),
            childColumns = arrayOf(PlaylistSongMapping.COLUMN_PLAYLIST_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(PlaylistSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_PATH),
            childColumns = arrayOf(PlaylistSongMapping.COLUMN_SONG_PATH),
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PlaylistSongMapping::class,
            parentColumns = arrayOf(PlaylistSongMapping.COLUMN_ID),
            childColumns = arrayOf(PlaylistSongMapping.COLUMN_NEXT_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(PlaylistSongMapping.COLUMN_PLAYLIST_ID),
        Index(PlaylistSongMapping.COLUMN_IS_HEAD),
        Index(PlaylistSongMapping.COLUMN_NEXT_ID),
    ],
)
data class PlaylistSongMapping(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_PLAYLIST_ID)
    val playlistId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String?,
    @ColumnInfo(COLUMN_SONG_PATH)
    val songPath: String?,
    @ColumnInfo(COLUMN_IS_HEAD)
    val isHead: Boolean,
    @ColumnInfo(COLUMN_NEXT_ID)
    val nextId: String?,
) {
    companion object {
        const val TABLE = "playlist_songs_mapping"
        const val COLUMN_ID = "id"
        const val COLUMN_PLAYLIST_ID = "playlist_id"
        const val COLUMN_SONG_ID = "song_id"
        const val COLUMN_SONG_PATH = "song_path"
        const val COLUMN_IS_HEAD = "is_head"
        const val COLUMN_NEXT_ID = "next_id"
    }
}

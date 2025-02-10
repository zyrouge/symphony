package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    SongQueueSongMapping.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = SongQueue::class,
            parentColumns = arrayOf(SongQueue.COLUMN_ID),
            childColumns = arrayOf(SongQueueSongMapping.COLUMN_QUEUE_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(SongQueueSongMapping.COLUMN_SONG_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = SongQueueSongMapping::class,
            parentColumns = arrayOf(SongQueueSongMapping.COLUMN_ID),
            childColumns = arrayOf(SongQueueSongMapping.COLUMN_NEXT_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(SongQueueSongMapping.COLUMN_QUEUE_ID),
        Index(SongQueueSongMapping.COLUMN_IS_HEAD),
        Index(SongQueueSongMapping.COLUMN_NEXT_ID),
    ],
)
data class SongQueueSongMapping(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_QUEUE_ID)
    val queueId: String,
    @ColumnInfo(COLUMN_SONG_ID)
    val songId: String?,
    @ColumnInfo(COLUMN_IS_HEAD)
    val isStart: Boolean,
    @ColumnInfo(COLUMN_NEXT_ID)
    val nextId: String?,
) {
    companion object {
        const val TABLE = "song_queue_songs_mapping"
        const val COLUMN_ID = "id"
        const val COLUMN_QUEUE_ID = "queue_id"
        const val COLUMN_SONG_ID = "song_id"
        const val COLUMN_IS_HEAD = "is_head"
        const val COLUMN_NEXT_ID = "next_id"
    }
}

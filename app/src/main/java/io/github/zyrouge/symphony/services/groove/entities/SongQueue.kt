package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Immutable
@Entity(
    SongQueue.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = SongQueueSongMapping::class,
            parentColumns = arrayOf(SongQueueSongMapping.COLUMN_ID),
            childColumns = arrayOf(SongQueue.COLUMN_PLAYING_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class SongQueue(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_PLAYING_ID)
    val playingId: String?,
    @ColumnInfo(COLUMN_PLAYING_TIMESTAMP)
    val playingTimestamp: Long?,
    @ColumnInfo(COLUMN_SHUFFLED)
    val shuffled: Boolean,
    @ColumnInfo(COLUMN_LOOP_MODE)
    val loopMode: LoopMode,
) {
    enum class LoopMode {
        None,
        Queue,
        Song;

        companion object {
            val values = enumValues<LoopMode>()
        }
    }

    data class AlongAttributes(
        @Embedded
        val entity: SongQueue,
        @Embedded
        val tracksCount: Int,
    ) {
        companion object {
            const val EMBEDDED_TRACKS_COUNT = "tracksCount"
        }
    }

    companion object {
        const val TABLE = "song_queue"
        const val COLUMN_ID = "id"
        const val COLUMN_PLAYING_ID = "playing_id"
        const val COLUMN_PLAYING_TIMESTAMP = "playing_timestamp"
        const val COLUMN_SHUFFLED = "shuffled"
        const val COLUMN_LOOP_MODE = "loop_mode"
    }
}

package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    SongQueue.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = SongQueue::class,
            parentColumns = arrayOf(SongQueue.COLUMN_ID),
            childColumns = arrayOf(SongQueue.COLUMN_ORIGINAL_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = SongQueueSongMapping::class,
            parentColumns = arrayOf(SongQueueSongMapping.COLUMN_ID),
            childColumns = arrayOf(SongQueue.COLUMN_PLAYING_ID),
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(SongQueue.COLUMN_ORIGINAL_ID),
        Index(SongQueue.COLUMN_INTERNAL_ID, unique = true),
        Index(SongQueue.COLUMN_PLAYING_ID),
    ],
)
data class SongQueue(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_ORIGINAL_ID)
    val originalId: String? = null,
    @ColumnInfo(COLUMN_INTERNAL_ID)
    val internalId: Int? = null,
    @ColumnInfo(COLUMN_PLAYING_ID)
    val playingId: String?,
    @ColumnInfo(COLUMN_PLAYING_IS_PLAYING)
    val isPlaying: Boolean,
    @ColumnInfo(COLUMN_PLAYING_TIMESTAMP)
    val playingTimestamp: Long,
    @ColumnInfo(COLUMN_PLAYING_TIMESTAMP_UPDATED_AT)
    val playingTimestampUpdatedAt: Long,
    @ColumnInfo(COLUMN_PLAYING_SPEED_INT)
    val playingSpeedInt: Int,
    @ColumnInfo(COLUMN_PLAYING_PITCH_INT)
    val playingPitchInt: Int,
    @ColumnInfo(COLUMN_SHUFFLED)
    val shuffled: Boolean,
    @ColumnInfo(COLUMN_LOOP_MODE)
    val loopMode: LoopMode,
    @ColumnInfo(COLUMN_SPEED_INT)
    val speedInt: Int,
    @ColumnInfo(COLUMN_PITCH_INT)
    val pitchInt: Int,
    @ColumnInfo(COLUMN_PAUSE_ON_SONG_END)
    val pauseOnSongEnd: Boolean,
    @ColumnInfo(COLUMN_SLEEP_TIMER_ENDS_AT)
    val sleepTimerEndsAt: Long?,
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
        val tracksCount: Int,
    ) {
        companion object {
            const val EMBEDDED_TRACKS_COUNT = "tracksCount"
        }
    }

    val speed get() = speedInt.toFloat() / SPEED_MULTIPLIER
    val pitch get() = pitchInt.toFloat() / PITCH_MULTIPLIER

    companion object {
        const val TABLE = "song_queue"
        const val COLUMN_ID = "id"
        const val COLUMN_ORIGINAL_ID = "original_id"
        const val COLUMN_INTERNAL_ID = "internal_id"
        const val COLUMN_PLAYING_ID = "playing_id"
        const val COLUMN_PLAYING_IS_PLAYING = "is_playing"
        const val COLUMN_PLAYING_SPEED_INT = "playing_speed_int"
        const val COLUMN_PLAYING_PITCH_INT = "playing_pitch_int"
        const val COLUMN_PLAYING_TIMESTAMP = "playing_timestamp"
        const val COLUMN_PLAYING_TIMESTAMP_UPDATED_AT = "playing_timestamp_updated_at"
        const val COLUMN_SHUFFLED = "shuffled"
        const val COLUMN_LOOP_MODE = "loop_mode"
        const val COLUMN_SPEED_INT = "speed_int"
        const val COLUMN_PITCH_INT = "pitch_int"
        const val COLUMN_PAUSE_ON_SONG_END = "pause_on_song_end"
        const val COLUMN_SLEEP_TIMER_ENDS_AT = "sleep_timer_ends_at"

        const val SPEED_MULTIPLIER = 100
        const val PITCH_MULTIPLIER = 100
    }
}

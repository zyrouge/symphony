package io.github.zyrouge.symphony.services.groove.entities

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.zyrouge.symphony.Symphony
import java.math.RoundingMode
import java.time.LocalDate

@Immutable
@Entity(
    Song.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = MediaTreeSongFile::class,
            parentColumns = arrayOf(MediaTreeSongFile.COLUMN_ID),
            childColumns = arrayOf(Song.COLUMN_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(Song.COLUMN_TITLE),
        Index(Song.COLUMN_PATH, unique = true),
    ],
)
data class Song(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_TITLE)
    val title: String,
    @ColumnInfo(COLUMN_TRACK_NUMBER)
    val trackNumber: Int?,
    @ColumnInfo(COLUMN_TRACK_TOTAL)
    val trackTotal: Int?,
    @ColumnInfo(COLUMN_DISC_NUMBER)
    val discNumber: Int?,
    @ColumnInfo(COLUMN_DISC_TOTAL)
    val discTotal: Int?,
    @ColumnInfo(COLUMN_DATE)
    val date: LocalDate?,
    @ColumnInfo(COLUMN_YEAR)
    val year: Int?,
    @ColumnInfo(COLUMN_DURATION)
    val duration: Long,
    @ColumnInfo(COLUMN_BITRATE)
    val bitrate: Long?,
    @ColumnInfo(COLUMN_SAMPLING_RATE)
    val samplingRate: Long?,
    @ColumnInfo(COLUMN_CHANNELS)
    val channels: Int?,
    @ColumnInfo(COLUMN_ENCODER)
    val encoder: String?,
    @ColumnInfo(COLUMN_DATE_MODIFIED)
    val dateModified: Long,
    @ColumnInfo(COLUMN_SIZE)
    val size: Long,
    @ColumnInfo(COLUMN_FILENAME)
    val filename: String,
    @ColumnInfo(COLUMN_URI)
    val uri: Uri,
    @ColumnInfo(COLUMN_PATH)
    val path: String,
) {
    data class AlongSongQueueMapping(
        @Embedded
        val entity: Song,
        @Embedded
        val mapping: SongQueueSongMapping,
    )

    data class AlongPlaylistMapping(
        @Embedded
        val song: Song,
        @Embedded
        val mapping: PlaylistSongMapping,
    )

    val bitrateK: Long? get() = bitrate?.let { it / 1000 }
    val samplingRateK: Float?
        get() = samplingRate?.let {
            (it.toFloat() / 1000)
                .toBigDecimal()
                .setScale(1, RoundingMode.CEILING)
                .toFloat()
        }

    fun toSamplingInfoString(symphony: Symphony): String? {
        val values = mutableListOf<String>()
        encoder?.let {
            values.add(it)
        }
        channels?.let {
            values.add(symphony.t.XChannels(it.toString()))
        }
        bitrateK?.let {
            values.add(buildString {
                append(symphony.t.XKbps(it.toString()))
            })
        }
        samplingRateK?.let {
            values.add(symphony.t.XKHz(it.toString()))
        }
        return when {
            values.isNotEmpty() -> values.joinToString(", ")
            else -> null
        }
    }

    companion object {
        const val TABLE = "songs"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TRACK_NUMBER = "track_number"
        const val COLUMN_TRACK_TOTAL = "track_total"
        const val COLUMN_DISC_NUMBER = "disc_number"
        const val COLUMN_DISC_TOTAL = "disc_total"
        const val COLUMN_DATE = "date"
        const val COLUMN_YEAR = "year"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_BITRATE = "bitrate"
        const val COLUMN_SAMPLING_RATE = "sampling_rate"
        const val COLUMN_CHANNELS = "channels"
        const val COLUMN_ENCODER = "encoder"
        const val COLUMN_DATE_MODIFIED = "date_modified"
        const val COLUMN_SIZE = "size"
        const val COLUMN_FILENAME = "filename"
        const val COLUMN_URI = "uri"
        const val COLUMN_PATH = "path"
    }
}

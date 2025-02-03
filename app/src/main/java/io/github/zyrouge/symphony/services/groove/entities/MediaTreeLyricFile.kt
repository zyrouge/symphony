package io.github.zyrouge.symphony.services.groove.entities

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    MediaTreeLyricFile.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = MediaTreeFolder::class,
            parentColumns = arrayOf(MediaTreeFolder.COLUMN_ID),
            childColumns = arrayOf(MediaTreeLyricFile.COLUMN_PARENT_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Playlist::class,
            parentColumns = arrayOf(Song.COLUMN_ID),
            childColumns = arrayOf(MediaTreeLyricFile.COLUMN_NAME),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(MediaTreeLyricFile.COLUMN_PARENT_ID),
        Index(MediaTreeLyricFile.COLUMN_NAME),
    ],
)
data class MediaTreeLyricFile(
    @PrimaryKey
    @ColumnInfo(COLUMN_ID)
    val id: String,
    @ColumnInfo(COLUMN_PARENT_ID)
    val parentId: String,
    @ColumnInfo(COLUMN_NAME)
    val name: String,
    @ColumnInfo(COLUMN_DATE_MODIFIED)
    val dateModified: Long,
    @ColumnInfo(COLUMN_URI)
    val uri: Uri,
) {
    companion object {
        const val TABLE = "media_tree_lyric_files"
        const val COLUMN_ID = "id"
        const val COLUMN_ROOT_ID = "root_id"
        const val COLUMN_PARENT_ID = "parent_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DATE_MODIFIED = "date_modified"
        const val COLUMN_URI = "uri"
    }
}

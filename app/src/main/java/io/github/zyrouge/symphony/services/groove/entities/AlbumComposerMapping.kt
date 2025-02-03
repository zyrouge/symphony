package io.github.zyrouge.symphony.services.groove.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Immutable
@Entity(
    AlbumComposerMapping.TABLE,
    primaryKeys = [AlbumComposerMapping.COLUMN_ALBUM_ID, AlbumComposerMapping.COLUMN_COMPOSER_ID],
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = arrayOf(Album.COLUMN_ID),
            childColumns = arrayOf(AlbumComposerMapping.COLUMN_ALBUM_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = arrayOf(Composer.COLUMN_ID),
            childColumns = arrayOf(AlbumComposerMapping.COLUMN_COMPOSER_ID),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(AlbumComposerMapping.COLUMN_ALBUM_ID),
        Index(AlbumComposerMapping.COLUMN_COMPOSER_ID),
    ],
)
data class AlbumComposerMapping(
    @ColumnInfo(COLUMN_ALBUM_ID)
    val albumId: String,
    @ColumnInfo(COLUMN_COMPOSER_ID)
    val composerId: String,
) {
    companion object {
        const val TABLE = "album_composer_mapping"
        const val COLUMN_ALBUM_ID = "album_id"
        const val COLUMN_COMPOSER_ID = "composer_id"
    }
}

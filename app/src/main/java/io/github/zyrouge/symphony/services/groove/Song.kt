package io.github.zyrouge.symphony.services.groove

import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import androidx.compose.runtime.Immutable
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.getColumnValue
import io.github.zyrouge.symphony.utils.getColumnValueNullable
import kotlin.io.path.Path

@Immutable
data class Song(
    val id: Long,
    val title: String,
    val trackNumber: Int?,
    val year: Int?,
    val duration: Long,
    val albumId: Long,
    val albumName: String?,
    val artistId: Long,
    val artistName: String?,
    val composer: String?,
    val additional: AdditionalMetadata,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val path: String,
) {
    @Immutable
    data class AdditionalMetadata(
        val albumArtist: String?,
        val genre: String?,
        val bitrate: Int?,
    ) {
        companion object {
            fun fromSongCacheAttributes(attributes: SongCache.Attributes) = AdditionalMetadata(
                albumArtist = attributes.albumArtist,
                bitrate = attributes.bitrate?.toInt(),
                genre = attributes.genre,
            )

            fun fetch(symphony: Symphony, id: Long): AdditionalMetadata {
                val retriever = MediaMetadataRetriever().apply {
                    setDataSource(symphony.applicationContext, buildUri(id))
                }
                return retriever.use {
                    AdditionalMetadata(
                        albumArtist = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                        bitrate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                            ?.toInt(),
                        genre = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                    )
                }
            }
        }
    }

    val filename = Path(path).fileName.toString()
    val uri: Uri get() = buildUri(id)

    fun getArtworkUri(symphony: Symphony) = symphony.groove.album.getAlbumArtworkUri(albumId)

    companion object {
        fun buildUri(id: Long) =
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())

        fun fromCursor(
            symphony: Symphony,
            cursor: Cursor,
            fetchCachedAttributes: (Long) -> SongCache.Attributes?,
        ): Song {
            val id = cursor.getColumnValue(AudioColumns._ID) {
                cursor.getLong(it)
            }
            val dateModified = cursor.getColumnValue(AudioColumns.DATE_MODIFIED) {
                cursor.getLong(it)
            }
            return Song(
                id = id,
                title = cursor.getColumnValue(AudioColumns.TITLE) {
                    cursor.getString(it)
                },
                trackNumber = cursor.getColumnValueNullable(AudioColumns.TRACK) {
                    cursor.getIntOrNull(it)
                },
                year = cursor.getColumnValueNullable(AudioColumns.YEAR) {
                    cursor.getIntOrNull(it)
                },
                duration = cursor.getColumnValue(AudioColumns.DURATION) {
                    cursor.getLong(it)
                },
                albumId = cursor.getColumnValue(AudioColumns.ALBUM_ID) {
                    cursor.getLong(it)
                },
                albumName = cursor.getColumnValueNullable(AudioColumns.ALBUM) {
                    cursor.getStringOrNull(it)
                },
                artistId = cursor.getColumnValue(AudioColumns.ARTIST_ID) {
                    cursor.getLong(it)
                },
                artistName = cursor.getColumnValueNullable(AudioColumns.ARTIST) {
                    cursor.getStringOrNull(it)
                },
                composer = cursor.getColumnValueNullable(AudioColumns.COMPOSER) {
                    cursor.getStringOrNull(it)
                },
                dateAdded = cursor.getColumnValue(AudioColumns.DATE_ADDED) {
                    cursor.getLong(it)
                },
                dateModified = dateModified,
                size = cursor.getColumnValue(AudioColumns.SIZE) {
                    cursor.getLong(it)
                },
                path = cursor.getColumnValue(AudioColumns.DATA) {
                    cursor.getString(it)
                },
                additional = fetchCachedAttributes(id)
                    ?.takeIf { it.lastModified == dateModified }
                    ?.let { AdditionalMetadata.fromSongCacheAttributes(it) }
                    ?: AdditionalMetadata.fetch(symphony, id),
            )
        }
    }
}

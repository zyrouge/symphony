package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist

@Dao
abstract class AlbumArtistMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(vararg entities: AlbumArtistMapping)

    @RawQuery
    protected abstract fun findArtistNamesByAlbumId(query: SimpleSQLiteQuery): List<String>

    fun findArtistNamesByAlbumId(albumId: String): List<String> {
        val query = "SELECT ${Artist.TABLE}.${Artist.COLUMN_NAME} FROM ${Artist.TABLE} " +
                "INNER JOIN ${AlbumArtistMapping.TABLE} ON ${Artist.TABLE}.${Artist.COLUMN_ID} = ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} " +
                "WHERE ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ?"
        return findArtistNamesByAlbumId(SimpleSQLiteQuery(query, arrayOf(albumId)))
    }
}

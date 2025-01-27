package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumStore {
    @Insert
    suspend fun insert(vararg entities: Album): List<String>

    @Update
    suspend fun update(vararg entities: Album): Int

    @Query("SELECT * FROM ${Album.TABLE} WHERE ${Album.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(name: String): Album?

    @RawQuery(observedEntities = [Album::class, AlbumArtistMapping::class, AlbumSongMapping::class])
    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<Album.AlongAttributes>>
}

fun AlbumStore.valuesAsFlow(
    sortBy: AlbumRepository.SortBy,
    sortReverse: Boolean,
): Flow<List<Album.AlongAttributes>> {
    val orderBy = when (sortBy) {
        AlbumRepository.SortBy.CUSTOM -> "${Album.TABLE}.${Album.COLUMN_ID}"
        AlbumRepository.SortBy.ALBUM_NAME -> "${Album.TABLE}.${Album.COLUMN_NAME}"
        AlbumRepository.SortBy.YEAR -> "${Album.TABLE}.${Album.COLUMN_START_YEAR}"
        AlbumRepository.SortBy.TRACKS_COUNT -> Album.AlongAttributes.EMBEDDED_TRACKS_COUNT
        AlbumRepository.SortBy.ARTISTS_COUNT -> Album.AlongAttributes.EMBEDDED_ARTISTS_COUNT
    }
    val orderDirection = if (sortReverse) "DESC" else "ASC"
    val query = "SELECT ${Album.TABLE}.*, " +
            "COUNT(${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_SONG_ID}) as ${Album.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
            "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID}) as ${Album.AlongAttributes.EMBEDDED_ARTISTS_COUNT} " +
            "FROM ${Album.TABLE} " +
            "LEFT JOIN ${AlbumSongMapping.TABLE} ON ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_ALBUM_ID} = ${Album.TABLE}.${Album.COLUMN_ID} " +
            "LEFT JOIN ${AlbumArtistMapping.TABLE} ON ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ${Album.TABLE}.${Album.COLUMN_ID} " +
            "ORDER BY $orderBy $orderDirection"
    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
}

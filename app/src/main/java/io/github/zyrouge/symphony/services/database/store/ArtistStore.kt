package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.ArtistRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistStore {
    @Insert
    suspend fun insert(vararg entities: Artist): List<String>

    @Update
    suspend fun update(vararg entities: Artist): Int

    @RawQuery(observedEntities = [Artist::class, ArtistSongMapping::class, AlbumArtistMapping::class])
    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<Artist.AlongAttributes>>

    @Query("SELECT ${Artist.COLUMN_ID}, ${Artist.COLUMN_NAME} FROM ${Artist.TABLE} WHERE ${Artist.COLUMN_NAME} in (:names)")
    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Artist.COLUMN_NAME) String,
            @MapColumn(Artist.COLUMN_ID) String>
}

fun ArtistStore.valuesAsFlow(
    sortBy: ArtistRepository.SortBy,
    sortReverse: Boolean,
): Flow<List<Artist.AlongAttributes>> {
    val orderBy = when (sortBy) {
        ArtistRepository.SortBy.CUSTOM -> "${Artist.TABLE}.${Artist.COLUMN_ID}"
        ArtistRepository.SortBy.ARTIST_NAME -> "${Artist.TABLE}.${Artist.COLUMN_NAME}"
        ArtistRepository.SortBy.TRACKS_COUNT -> Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT
        ArtistRepository.SortBy.ALBUMS_COUNT -> Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT
    }
    val orderDirection = if (sortReverse) "DESC" else "ASC"
    val query = "SELECT ${Artist.TABLE}.*, " +
            "COUNT(${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_SONG_ID}) as ${Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
            "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID}) as ${Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT} " +
            "FROM ${Artist.TABLE} " +
            "LEFT JOIN ${ArtistSongMapping.TABLE} ON ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_ARTIST_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID} " +
            "LEFT JOIN ${AlbumArtistMapping.TABLE} ON ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID}" +
            "ORDER BY $orderBy $orderDirection"
    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
}

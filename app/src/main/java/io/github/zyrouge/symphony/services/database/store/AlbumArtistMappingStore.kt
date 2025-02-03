package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.AlbumArtistRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumArtistMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: AlbumArtistMapping)

    @RawQuery(observedEntities = [AlbumArtistMapping::class, Artist::class, ArtistSongMapping::class])
    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<Artist.AlongAttributes>>
}

fun AlbumArtistMappingStore.valuesAsFlow(
    sortBy: AlbumArtistRepository.SortBy,
    sortReverse: Boolean,
): Flow<List<Artist.AlongAttributes>> {
    val orderBy = when (sortBy) {
        AlbumArtistRepository.SortBy.CUSTOM -> "${Artist.TABLE}.${Artist.COLUMN_ID}"
        AlbumArtistRepository.SortBy.ARTIST_NAME -> "${Artist.TABLE}.${Artist.COLUMN_NAME}"
        AlbumArtistRepository.SortBy.TRACKS_COUNT -> Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT
        AlbumArtistRepository.SortBy.ALBUMS_COUNT -> Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT
    }
    val orderDirection = if (sortReverse) "DESC" else "ASC"
    val query = "SELECT ${Artist.TABLE}.*, " +
            "COUNT(${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_SONG_ID}) as ${Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
            "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID}) as ${Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT} " +
            "FROM ${Artist.TABLE} " +
            "LEFT JOIN ${ArtistSongMapping.TABLE} ON ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_ARTIST_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID} " +
            "LEFT JOIN ${AlbumArtistMapping.TABLE} ON ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID} " +
            "WHERE ${AlbumArtistMapping.COLUMN_IS_ALBUM_ARTIST} = 1 " +
            "ORDER BY $orderBy $orderDirection"
    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
}

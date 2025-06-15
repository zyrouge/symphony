package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.ArtistRepository
import io.github.zyrouge.symphony.utils.builtin.sqlqph
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ArtistStore {
    @Insert
    abstract suspend fun insert(vararg entities: Artist): List<String>

    @Update
    abstract suspend fun update(vararg entities: Artist): Int

    @RawQuery(observedEntities = [Artist::class, ArtistSongMapping::class, AlbumArtistMapping::class])
    protected abstract fun findByIdAsFlow(query: SupportSQLiteQuery): Flow<Artist.AlongAttributes?>

    fun findByIdAsFlow(id: String): Flow<Artist.AlongAttributes?> {
        val query = "SELECT ${Artist.TABLE}.*, " +
                "COUNT(${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_SONG_ID}) as ${Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
                "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID}) as ${Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT} " +
                "FROM ${Artist.TABLE} " +
                "LEFT JOIN ${AlbumSongMapping.TABLE} ON ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_ALBUM_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID} " +
                "LEFT JOIN ${AlbumArtistMapping.TABLE} ON ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID} " +
                "WHERE ${Artist.COLUMN_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(id)
        return findByIdAsFlow(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [Artist::class, ArtistSongMapping::class, AlbumArtistMapping::class])
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<Artist.AlongAttributes>>

    fun valuesAsFlow(
        sortBy: ArtistRepository.SortBy,
        sortReverse: Boolean,
        albumId: String? = null,
        onlyAlbumArtists: Boolean = false,
    ): Flow<List<Artist.AlongAttributes>> {
        val orderBy = when (sortBy) {
            ArtistRepository.SortBy.CUSTOM -> "${Artist.TABLE}.${Artist.COLUMN_ID}"
            ArtistRepository.SortBy.ARTIST_NAME -> "${Artist.TABLE}.${Artist.COLUMN_NAME}"
            ArtistRepository.SortBy.TRACKS_COUNT -> Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT
            ArtistRepository.SortBy.ALBUMS_COUNT -> Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val albumArtistMappingJoin = "" +
                (if (albumId != null) "${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ? " else "") +
                (if (onlyAlbumArtists) "${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_IS_ALBUM_ARTIST} = 1 " else "") +
                "${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID}"
        val query = "SELECT ${Artist.TABLE}.*, " +
                "COUNT(${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_SONG_ID}) as ${Artist.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
                "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID}) as ${Artist.AlongAttributes.EMBEDDED_ALBUMS_COUNT} " +
                "FROM ${Artist.TABLE} " +
                "LEFT JOIN ${ArtistSongMapping.TABLE} ON ${ArtistSongMapping.TABLE}.${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_ARTIST_ID} = ${Artist.TABLE}.${Artist.COLUMN_ID} " +
                "LEFT JOIN ${AlbumArtistMapping.TABLE} ON $albumArtistMappingJoin " +
                "ORDER BY $orderBy $orderDirection"
        val args = mutableListOf<Any>()
        if (albumId != null) {
            args.add(albumId)
        }
        return valuesAsFlow(SimpleSQLiteQuery(query, args.toTypedArray()))
    }

    @RawQuery
    protected abstract fun entriesByNameNameIdMapped(query: SimpleSQLiteQuery): Map<
            @MapColumn(Artist.COLUMN_NAME) String,
            @MapColumn(Artist.COLUMN_ID) String>

    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Artist.COLUMN_NAME) String,
            @MapColumn(Artist.COLUMN_ID) String> {
        val query = "SELECT ${Artist.COLUMN_ID}, ${Artist.COLUMN_NAME} " +
                "FROM ${Artist.TABLE} " +
                "WHERE ${Artist.COLUMN_NAME} in (${sqlqph(names.size)})"
        val args = arrayOf(*names.toTypedArray())
        return entriesByNameNameIdMapped(SimpleSQLiteQuery(query, args))
    }
}

package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AlbumStore {
    @Insert
    abstract suspend fun insert(vararg entities: Album): List<String>

    @Update
    abstract suspend fun update(vararg entities: Album): Int

    @RawQuery
    protected abstract fun findByName(query: SimpleSQLiteQuery): Album?

    fun findByName(name: String): Album? {
        val query = "SELECT * FROM ${Album.TABLE} WHERE ${Album.COLUMN_NAME} = ? LIMIT 1"
        val args = arrayOf(name)
        return findByName(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [Album::class, AlbumArtistMapping::class, AlbumSongMapping::class])
    protected abstract fun findByIdAsFlow(query: SupportSQLiteQuery): Flow<Album.AlongAttributes?>

    fun findByIdAsFlow(id: String): Flow<Album.AlongAttributes?> {
        val query = "SELECT ${Album.TABLE}.*, " +
                "COUNT(${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_SONG_ID}) as ${Album.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
                "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID}) as ${Album.AlongAttributes.EMBEDDED_ARTISTS_COUNT} " +
                "FROM ${Album.TABLE} " +
                "LEFT JOIN ${AlbumSongMapping.TABLE} ON ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_ALBUM_ID} = ${Album.TABLE}.${Album.COLUMN_ID} " +
                "LEFT JOIN ${AlbumArtistMapping.TABLE} ON ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ${Album.TABLE}.${Album.COLUMN_ID} " +
                "WHERE ${Album.COLUMN_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(id)
        return findByIdAsFlow(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [Album::class, AlbumArtistMapping::class, AlbumSongMapping::class])
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<Album.AlongAttributes>>

    fun valuesAsFlow(
        sortBy: AlbumRepository.SortBy,
        sortReverse: Boolean,
        artistId: String? = null,
    ): Flow<List<Album.AlongAttributes>> {
        val aliasFirstArtist = "firstArtist"
        val embeddedArtistName = "firstArtistName"
        val orderBy = when (sortBy) {
            AlbumRepository.SortBy.CUSTOM -> "${Album.TABLE}.${Album.COLUMN_ID}"
            AlbumRepository.SortBy.ALBUM_NAME -> "${Album.TABLE}.${Album.COLUMN_NAME}"
            AlbumRepository.SortBy.ARTIST_NAME -> embeddedArtistName
            AlbumRepository.SortBy.YEAR -> "${Album.TABLE}.${Album.COLUMN_START_YEAR}"
            AlbumRepository.SortBy.TRACKS_COUNT -> Album.AlongAttributes.EMBEDDED_TRACKS_COUNT
            AlbumRepository.SortBy.ARTISTS_COUNT -> Album.AlongAttributes.EMBEDDED_ARTISTS_COUNT
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val artistQuery = "SELECT" +
                "TOP 1 ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} " +
                "FROM ${AlbumArtistMapping.TABLE} " +
                "WHERE ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ${Album.COLUMN_ID} " +
                "ORDER BY ${AlbumArtistMapping.COLUMN_IS_ALBUM_ARTIST} DESC"
        val albumArtistMappingJoin = "" +
                (if (artistId != null) "${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} = ? " else "") +
                "${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ${Album.TABLE}.${Album.COLUMN_ID}"
        val query = "SELECT ${Album.TABLE}.*, " +
                "COUNT(${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_SONG_ID}) as ${Album.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
                "COUNT(${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID}) as ${Album.AlongAttributes.EMBEDDED_ARTISTS_COUNT}, " +
                "$aliasFirstArtist.${Artist.COLUMN_NAME} as $embeddedArtistName" +
                "FROM ${Album.TABLE} " +
                "LEFT JOIN ${AlbumSongMapping.TABLE} ON ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_ALBUM_ID} = ${Album.TABLE}.${Album.COLUMN_ID} " +
                "LEFT JOIN ${AlbumArtistMapping.TABLE} ON $albumArtistMappingJoin " +
                "LEFT JOIN ${Artist.TABLE} $aliasFirstArtist ON ${Artist.TABLE}.${Artist.COLUMN_ID} = ($artistQuery) " +
                "ORDER BY $orderBy $orderDirection"
        return valuesAsFlow(SimpleSQLiteQuery(query))
    }
}

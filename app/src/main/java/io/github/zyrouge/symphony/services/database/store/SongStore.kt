package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Composer
import io.github.zyrouge.symphony.services.groove.entities.ComposerSongMapping
import io.github.zyrouge.symphony.services.groove.entities.GenreSongMapping
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.utils.builtin.sqlqph
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongStore {
    @Insert
    abstract suspend fun insert(vararg entities: Song): List<String>

    @Update
    abstract suspend fun update(vararg entities: Song): Int

    @RawQuery
    protected abstract suspend fun delete(query: SimpleSQLiteQuery): Int

    suspend fun delete(vararg ids: String): Int {
        val query = "DELETE FROM ${Song.TABLE} WHERE ${Song.COLUMN_ID} IN (${sqlqph(ids.size)})"
        return delete(SimpleSQLiteQuery(query, ids))
    }

    @RawQuery
    protected abstract fun findByPath(query: SimpleSQLiteQuery): Song?

    fun findByPath(path: String): Song? {
        val query = "SELECT * FROM ${Song.TABLE} WHERE ${Song.COLUMN_PATH} = ? LIMIT 1"
        val args = arrayOf(path)
        return findByPath(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun ids(query: SimpleSQLiteQuery): List<String>

    fun ids(): List<String> {
        val query = "SELECT ${Song.COLUMN_ID} FROM ${Song.TABLE}"
        return ids(SimpleSQLiteQuery(query))
    }

    @RawQuery(
        observedEntities = [
            AlbumSongMapping::class,
            ArtistSongMapping::class,
            ComposerSongMapping::class,
            GenreSongMapping::class,
            PlaylistSongMapping::class,
            Song::class,
        ]
    )
    protected abstract fun entriesAsFlow(query: SupportSQLiteQuery): Flow<Map<@MapColumn(Song.COLUMN_ID) String, Song>>

    @RawQuery(
        observedEntities = [
            AlbumSongMapping::class,
            ArtistSongMapping::class,
            ComposerSongMapping::class,
            GenreSongMapping::class,
            PlaylistSongMapping::class,
            Song::class,
        ]
    )
    internal abstract fun entriesAsPlaylistSongMappedAsFlowRaw(query: SupportSQLiteQuery): Flow<
            Map<@MapColumn(Song.COLUMN_ID) String, Song.AlongPlaylistMapping>>

    fun valuesQuery(
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
        additionalClauseAfterSongSelect: String = "",
        additionalClauseBeforeJoins: String = "",
        additionalArgsBeforeJoins: Array<Any?> = emptyArray(),
        overrideOrderBy: String? = null,
    ): SupportSQLiteQuery {
        val aliasFirstAlbumArtist = "firstAlbumArtist"
        val embeddedFirstArtistName = "firstArtistName"
        val embeddedFirstAlbumName = "firstAlbumName"
        val embeddedFirstAlbumArtistName = "firstAlbumArtistName"
        val embeddedFirstComposerName = "firstComposerName"
        val orderBy = overrideOrderBy ?: when (sortBy) {
            SongRepository.SortBy.CUSTOM -> "${Song.TABLE}.${Song.COLUMN_ID}"
            SongRepository.SortBy.TITLE -> "${Song.TABLE}.${Song.COLUMN_ID}"
            SongRepository.SortBy.ARTIST -> embeddedFirstArtistName
            SongRepository.SortBy.ALBUM -> embeddedFirstAlbumName
            SongRepository.SortBy.DURATION -> "${Song.TABLE}.${Song.COLUMN_ID}"
            SongRepository.SortBy.DATE_MODIFIED -> "${Song.TABLE}.${Song.COLUMN_ID}"
            SongRepository.SortBy.COMPOSER -> embeddedFirstComposerName
            SongRepository.SortBy.ALBUM_ARTIST -> embeddedFirstAlbumArtistName
            SongRepository.SortBy.YEAR -> "${Song.TABLE}.${Song.COLUMN_YEAR}"
            SongRepository.SortBy.FILENAME -> "${Song.TABLE}.${Song.COLUMN_FILENAME}"
            SongRepository.SortBy.TRACK_NUMBER -> "${Song.TABLE}.${Song.COLUMN_TRACK_NUMBER}"
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val artistQuery = "SELECT" +
                "TOP 1 ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_ARTIST_ID} " +
                "FROM ${ArtistSongMapping.TABLE} " +
                "WHERE ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID}"
        val albumQuery = "SELECT" +
                "TOP 1 ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_ALBUM_ID} " +
                "FROM ${AlbumSongMapping.TABLE} " +
                "WHERE ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID}"
        val albumArtistQuery = "SELECT " +
                "TOP 1 ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ARTIST_ID} " +
                "FROM ${AlbumArtistMapping.TABLE} " +
                "WHERE ${AlbumArtistMapping.TABLE}.${AlbumArtistMapping.COLUMN_ALBUM_ID} = ${Album.COLUMN_ID}"
        val composerQuery = "SELECT " +
                "TOP 1 ${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_COMPOSER_ID} " +
                "FROM ${ComposerSongMapping.TABLE} " +
                "WHERE ${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID}"
        val query = "SELECT ${Song.TABLE}.*, " +
                additionalClauseAfterSongSelect +
                "${Artist.TABLE}.${Artist.COLUMN_NAME} as $embeddedFirstArtistName, " +
                "${Album.TABLE}.${Album.COLUMN_NAME} as $embeddedFirstAlbumName, " +
                "$aliasFirstAlbumArtist.${Artist.COLUMN_NAME} as $embeddedFirstAlbumArtistName, " +
                "${Composer.TABLE}.${Composer.COLUMN_NAME} as $embeddedFirstComposerName " +
                "FROM ${Song.TABLE} " +
                additionalClauseBeforeJoins +
                "LEFT JOIN ${Artist.TABLE} ON ${Artist.TABLE}.${Artist.COLUMN_ID} = ($artistQuery)" +
                "LEFT JOIN ${Album.TABLE} ON ${Album.TABLE}.${Album.COLUMN_ID} = ($albumQuery)" +
                "LEFT JOIN ${Artist.TABLE} $aliasFirstAlbumArtist ON ${Artist.TABLE}.${Artist.COLUMN_ID} = ($albumArtistQuery)" +
                "LEFT JOIN ${Composer.TABLE} ON ${Composer.TABLE}.${Composer.COLUMN_ID} = ($composerQuery)" +
                "ORDER BY $orderBy $orderDirection"
        val args = additionalArgsBeforeJoins
        return SimpleSQLiteQuery(query, args)
    }

    @RawQuery
    protected abstract fun values(query: SupportSQLiteQuery): List<Song>

    fun values(
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
        additionalClauseAfterSongSelect: String = "",
        additionalClauseBeforeJoins: String = "",
        additionalArgsBeforeJoins: Array<Any?> = emptyArray(),
        overrideOrderBy: String? = null,
    ): List<Song> {
        val query = valuesQuery(
            sortBy = sortBy,
            sortReverse = sortReverse,
            additionalClauseAfterSongSelect = additionalClauseAfterSongSelect,
            additionalClauseBeforeJoins = additionalClauseBeforeJoins,
            additionalArgsBeforeJoins = additionalArgsBeforeJoins,
            overrideOrderBy = overrideOrderBy,
        )
        return values(query)
    }

    @RawQuery(
        observedEntities = [
            AlbumSongMapping::class,
            ArtistSongMapping::class,
            ComposerSongMapping::class,
            GenreSongMapping::class,
            PlaylistSongMapping::class,
            Song::class,
        ]
    )
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<Song>>

    fun valuesAsFlow(
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
        additionalClauseAfterSongSelect: String = "",
        additionalClauseBeforeJoins: String = "",
        additionalArgsBeforeJoins: Array<Any?> = emptyArray(),
        overrideOrderBy: String? = null,
    ): Flow<List<Song>> {
        val query = valuesQuery(
            sortBy = sortBy,
            sortReverse = sortReverse,
            additionalClauseAfterSongSelect = additionalClauseAfterSongSelect,
            additionalClauseBeforeJoins = additionalClauseBeforeJoins,
            additionalArgsBeforeJoins = additionalArgsBeforeJoins,
            overrideOrderBy = overrideOrderBy,
        )
        return valuesAsFlow(query)
    }
}

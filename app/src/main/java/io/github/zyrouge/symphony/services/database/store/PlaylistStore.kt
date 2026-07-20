package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaylistStore {
    @Insert
    abstract suspend fun insert(vararg entities: Playlist)

    @Update
    abstract suspend fun update(vararg entities: Playlist): Int

    @Query("DELETE FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_ID} = :id")
    abstract suspend fun delete(id: String): Int

    @RawQuery(observedEntities = [Playlist::class, PlaylistSongMapping::class])
    protected abstract fun findByIdAsFlow(query: SupportSQLiteQuery): Flow<Playlist.AlongAttributes?>

    fun findByIdAsFlow(id: String): Flow<Playlist.AlongAttributes?> {
        val query = "SELECT ${Playlist.TABLE}.*, " +
                "COUNT(${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID}) as ${Playlist.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
                "FROM ${Playlist.TABLE} " +
                "LEFT JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ${Playlist.TABLE}.${Playlist.COLUMN_ID} " +
                "WHERE ${Playlist.TABLE}.${Playlist.COLUMN_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(id)
        return findByIdAsFlow(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findById(query: SimpleSQLiteQuery): Playlist?

    fun findById(id: String): Playlist? {
        val query = "SELECT * FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_ID} = ? LIMIT 1"
        val args = arrayOf(id)
        return findById(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun search(query: SimpleSQLiteQuery): List<Playlist>

    fun search(terms: String): List<Playlist> {
        val likeTerm = "%$terms%"
        val query = "SELECT * FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_TITLE} LIKE ?"
        return search(SimpleSQLiteQuery(query, arrayOf(likeTerm)))
    }

    @RawQuery
    protected abstract fun findByInternalId(query: SimpleSQLiteQuery): Playlist?

    fun findByInternalId(internalId: Int): Playlist? {
        val query = "SELECT * FROM ${Playlist.TABLE} " +
                "WHERE ${Playlist.COLUMN_INTERNAL_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(internalId)
        return findByInternalId(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun valuesLocalOnly(query: SimpleSQLiteQuery): List<Playlist>

    fun valuesLocalOnly(): List<Playlist> {
        val query = "SELECT * FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_URI} != NULL"
        return valuesLocalOnly(SimpleSQLiteQuery(query))
    }

    @RawQuery(observedEntities = [Playlist::class, PlaylistSongMapping::class])
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<Playlist.AlongAttributes>>

    fun valuesAsFlow(
        sortBy: PlaylistRepository.SortBy,
        sortReverse: Boolean,
    ): Flow<List<Playlist.AlongAttributes>> {
        val orderBy = when (sortBy) {
            PlaylistRepository.SortBy.CUSTOM -> "${Playlist.TABLE}.${Playlist.COLUMN_ID}"
            PlaylistRepository.SortBy.TITLE -> "${Playlist.TABLE}.${Playlist.COLUMN_TITLE}"
            PlaylistRepository.SortBy.TRACKS_COUNT -> Playlist.AlongAttributes.EMBEDDED_TRACKS_COUNT
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val query = "SELECT ${Playlist.TABLE}.*, " +
                "COUNT(${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID}) as ${Playlist.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
                "FROM ${Playlist.TABLE} " +
                "LEFT JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ${Playlist.TABLE}.${Playlist.COLUMN_ID} " +
                "ORDER BY $orderBy $orderDirection"
        return valuesAsFlow(SimpleSQLiteQuery(query))
    }
}

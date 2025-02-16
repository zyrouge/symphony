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
interface PlaylistStore {
    @Insert
    suspend fun insert(vararg entities: Playlist): List<String>

    @Update
    suspend fun update(vararg entities: Playlist): Int

    @Query("DELETE FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_ID} = :id")
    suspend fun delete(id: String): Int

    @RawQuery(observedEntities = [Playlist::class, PlaylistSongMapping::class])
    fun findByIdAsFlowRaw(query: SupportSQLiteQuery): Flow<Playlist.AlongAttributes?>

    @Query("SELECT * FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_URI} != NULL")
    fun valuesLocalOnly(): List<Playlist>

    @RawQuery(observedEntities = [Playlist::class, PlaylistSongMapping::class])
    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<Playlist.AlongAttributes>>
}

fun PlaylistStore.findByIdAsFlow(id: String): Flow<Playlist.AlongAttributes?> {
    val query = "SELECT ${Playlist.TABLE}.*, " +
            "COUNT(${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID}) as ${Playlist.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
            "FROM ${Playlist.TABLE} " +
            "LEFT JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ${Playlist.TABLE}.${Playlist.COLUMN_ID} " +
            "WHERE ${Playlist.TABLE}.${Playlist.COLUMN_ID} = ? " +
            "LIMIT 1"
    val args = arrayOf(id)
    return findByIdAsFlowRaw(SimpleSQLiteQuery(query, args))
}

fun PlaylistStore.valuesAsFlow(
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
    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
}

package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.utils.builtin.sqlqph
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Dao
abstract class PlaylistSongMappingStore {
    @Insert
    abstract suspend fun insert(vararg entities: PlaylistSongMapping)

    @Update
    abstract suspend fun update(vararg entities: PlaylistSongMapping)

    @RawQuery
    protected abstract suspend fun delete(query: SimpleSQLiteQuery): Int

    suspend fun delete(playlistId: String, ids: Collection<String>): Int {
        val query = "DELETE FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? " +
                "AND ${PlaylistSongMapping.COLUMN_ID} IN (${sqlqph(ids.size)})"
        val args = arrayOf(playlistId, *ids.toTypedArray())
        return delete(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    abstract suspend fun deleteAll(query: SimpleSQLiteQuery): Int

    suspend fun deleteAll(vararg playlistIds: String): Int {
        val query = "DELETE FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.COLUMN_PLAYLIST_ID} IN (${sqlqph(playlistIds.size)})"
        return deleteAll(SimpleSQLiteQuery(query, playlistIds))
    }

    @RawQuery(observedEntities = [SongArtworkIndex::class, PlaylistSongMapping::class])
    protected abstract fun findTop4SongArtworksAsFlowRaw(query: SimpleSQLiteQuery): Flow<List<SongArtworkIndex>>

    fun findTop4SongArtworksAsFlow(playlistId: String): Flow<List<SongArtworkIndex>> {
        val query = "SELECT ${SongArtworkIndex.TABLE}.* " +
                "FROM ${SongArtworkIndex.TABLE} " +
                "LEFT JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ?" +
                "AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} = ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_SONG_ID} " +
                "WHERE ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_FILE} != null " +
                "ORDER BY DESC " +
                "LIMIT 4"
        val args = arrayOf(playlistId)
        return findTop4SongArtworksAsFlowRaw(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [PlaylistSongMapping::class, Playlist::class])
    protected abstract fun findSongIdsByPlaylistInternalIdAsFlowRaw(query: SimpleSQLiteQuery): Flow<List<String>>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun findSongIdsByPlaylistInternalIdAsFlow(playlistInternalId: Int): Flow<List<String>> {
        val query = "SELECT ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} " +
                "FROM ${PlaylistSongMapping.TABLE} " +
                "LEFT JOIN ${Playlist.TABLE} ON ${Playlist.TABLE}.${Playlist.COLUMN_INTERNAL_ID} = ?" +
                "AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ${Playlist.TABLE}.${Playlist.COLUMN_ID} "
        val args = arrayOf(playlistInternalId)
        return findSongIdsByPlaylistInternalIdAsFlowRaw(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findById(query: SupportSQLiteQuery): Song.AlongPlaylistMapping?

    fun findById(playlistId: String, id: String?): Song.AlongPlaylistMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${PlaylistSongMapping.TABLE}.* " +
                "FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(playlistId, id)
        return findById(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findByNextId(query: SupportSQLiteQuery): Song.AlongPlaylistMapping?

    fun findByNextId(playlistId: String, nextId: String?): Song.AlongPlaylistMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${PlaylistSongMapping.TABLE}.* " +
                "FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_NEXT_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(playlistId, nextId)
        return findByNextId(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findHead(query: SupportSQLiteQuery): Song.AlongPlaylistMapping?

    fun findHead(playlistId: String): Song.AlongPlaylistMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${PlaylistSongMapping.TABLE}.* " +
                "FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_IS_HEAD} = true " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(playlistId)
        return findHead(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesByIds(query: SupportSQLiteQuery): Map<
            @MapColumn(PlaylistSongMapping.COLUMN_ID) String, Song.AlongPlaylistMapping>

    fun entriesByIds(playlistId: String, songMappingIds: List<String>): Map<
            String, Song.AlongPlaylistMapping> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${PlaylistSongMapping.TABLE}.* " +
                "FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? " +
                "AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_ID} " +
                "IN (${sqlqph(songMappingIds.size)}) " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(playlistId, *songMappingIds.toTypedArray())
        return entriesByIds(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesByNextIds(query: SupportSQLiteQuery): Map<
            @MapColumn(PlaylistSongMapping.COLUMN_NEXT_ID) String, Song.AlongPlaylistMapping>

    fun entriesByNextIds(playlistId: String, songMappingIds: List<String>): Map<
            String, Song.AlongPlaylistMapping> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${PlaylistSongMapping.TABLE}.* " +
                "FROM ${PlaylistSongMapping.TABLE} " +
                "WHERE ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? " +
                "AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_NEXT_ID} " +
                "IN (${sqlqph(songMappingIds.size)}) " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(playlistId, *songMappingIds.toTypedArray())
        return entriesByNextIds(SimpleSQLiteQuery(query, args))
    }

    fun valuesMapped(
        songStore: SongStore,
        id: String,
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
    ): List<Song> {
        val query = songStore.valuesQuery(
            sortBy,
            sortReverse,
            additionalClauseBeforeJoins = "JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ?" +
                    "AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID} ",
            additionalArgsBeforeJoins = arrayOf(id),
        )
        val entries = songStore.entriesAsPlaylistSongMapped(query)
        return transformEntriesAsValues(entries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun valuesMappedAsFlow(
        songStore: SongStore,
        id: String,
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
    ): Flow<List<Song>> {
        val query = songStore.valuesQuery(
            sortBy,
            sortReverse,
            additionalClauseBeforeJoins = "JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ?" +
                    "AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID} ",
            additionalArgsBeforeJoins = arrayOf(id),
        )
        val entries = songStore.entriesAsPlaylistSongMappedAsFlow(query)
        return entries.mapLatest { transformEntriesAsValues(it) }
    }

    fun transformEntriesAsValues(entries: Map<String, Song.AlongPlaylistMapping>): List<Song> {
        val list = mutableListOf<Song>()
        var head = entries.firstNotNullOfOrNull {
            when {
                it.value.mapping.isHead -> it.value
                else -> null
            }
        }
        while (head != null) {
            list.add(head.song)
            head = entries[head.mapping.nextId]
        }
        return list.toList()
    }
}

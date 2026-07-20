package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping
import io.github.zyrouge.symphony.utils.builtin.sqlqph
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Dao
abstract class SongQueueSongMappingStore {
    @Insert
    abstract suspend fun insert(vararg entities: SongQueueSongMapping)

    @Update
    abstract suspend fun update(vararg entities: SongQueueSongMapping)

    @RawQuery
    protected abstract suspend fun delete(query: SimpleSQLiteQuery): Int

    suspend fun delete(queueId: String, ids: Collection<String>): Int {
        val query = "DELETE FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
                "AND ${SongQueueSongMapping.COLUMN_ID} IN (${sqlqph(ids.size)})"
        val args = arrayOf(queueId, *ids.toTypedArray())
        return delete(SimpleSQLiteQuery(query, args))
    }

    suspend fun deleteAll(queueId: String): Int {
        val query = "DELETE FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? "
        val args = arrayOf(queueId)
        return delete(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findById(query: SupportSQLiteQuery): Song.AlongSongQueueMapping?

    fun findById(queueId: String, id: String): Song.AlongSongQueueMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId, id)
        return findById(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [SongQueueSongMapping::class, Song::class])
    protected abstract fun findByIdAsFlow(query: SupportSQLiteQuery): Flow<Song.AlongSongQueueMapping?>

    fun findByIdAsFlow(queueId: String, id: String): Flow<Song.AlongSongQueueMapping?> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId, id)
        return findByIdAsFlow(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findByNextId(query: SupportSQLiteQuery): Song.AlongSongQueueMapping?

    fun findByNextId(queueId: String, nextId: String?): Song.AlongSongQueueMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_NEXT_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId, nextId)
        return findByNextId(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findBySongId(query: SupportSQLiteQuery): Song.AlongSongQueueMapping?

    fun findBySongId(queueId: String, songId: String?): Song.AlongSongQueueMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId, songId)
        return findBySongId(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findHead(query: SupportSQLiteQuery): Song.AlongSongQueueMapping?

    fun findHead(queueId: String): Song.AlongSongQueueMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} = true " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId)
        return findHead(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entries(query: SupportSQLiteQuery): Map<
            @MapColumn(SongQueueSongMapping.COLUMN_ID) String, Song.AlongSongQueueMapping>

    fun entries(queueId: String): Map<
            String, Song.AlongSongQueueMapping> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(queueId)
        return entries(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesByIds(query: SupportSQLiteQuery): Map<
            @MapColumn(SongQueueSongMapping.COLUMN_ID) String, Song.AlongSongQueueMapping>

    fun entriesByIds(queueId: String, songMappingIds: List<String>): Map<
            String, Song.AlongSongQueueMapping> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
                "AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_ID} " +
                "IN (${sqlqph(songMappingIds.size)}) " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(queueId, *songMappingIds.toTypedArray())
        return entriesByIds(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesByNextIds(query: SupportSQLiteQuery): Map<
            @MapColumn(SongQueueSongMapping.COLUMN_NEXT_ID) String, Song.AlongSongQueueMapping>

    fun entriesByNextIds(queueId: String, songMappingIds: List<String>): Map<
            String, Song.AlongSongQueueMapping> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
                "AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_NEXT_ID} " +
                "IN (${sqlqph(songMappingIds.size)}) " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(queueId, *songMappingIds.toTypedArray())
        return entriesByNextIds(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesBySongIds(query: SupportSQLiteQuery): Map<
            @MapColumn(SongQueueSongMapping.COLUMN_NEXT_ID) String, Song.AlongSongQueueMapping>

    fun entriesBySongIds(queueId: String, songIds: List<String>): Map<
            String, Song.AlongSongQueueMapping> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
                "AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} " +
                "IN (${sqlqph(songIds.size)}) " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(queueId, *songIds.toTypedArray())
        return entriesBySongIds(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [Song::class, SongQueueSongMapping::class])
    protected abstract fun entriesAsFlowRaw(query: SupportSQLiteQuery): Flow<
            Map<@MapColumn(SongQueueSongMapping.COLUMN_ID) String, Song.AlongSongQueueMapping>>

    fun entriesAsFlow(queueId: String): Flow<Map<String, Song.AlongSongQueueMapping>> {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} " +
                "ORDER BY ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} DESC"
        val args = arrayOf(queueId)
        return entriesAsFlowRaw(SimpleSQLiteQuery(query, args))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun transformEntriesAsValues(entries: Map<String, Song.AlongSongQueueMapping>): List<Song.AlongSongQueueMapping> {
        val list = mutableListOf<Song.AlongSongQueueMapping>()
        var head = entries.firstNotNullOfOrNull {
            when {
                it.value.mapping.isHead -> it.value
                else -> null
            }
        }
        while (head != null) {
            list.add(head)
            head = entries[head.mapping.nextId]
        }
        return list.toList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun transformEntriesAsValuesFlow(entriesFlow: Flow<Map<String, Song.AlongSongQueueMapping>>): Flow<List<Song.AlongSongQueueMapping>> {
        return entriesFlow.mapLatest { transformEntriesAsValues(it) }
    }

    fun values(queueId: String): List<Song.AlongSongQueueMapping> {
        val entries = entries(queueId)
        return transformEntriesAsValues(entries)
    }

    fun valuesAsFlow(queueId: String): Flow<List<Song.AlongSongQueueMapping>> {
        val entries = entriesAsFlow(queueId)
        return transformEntriesAsValuesFlow(entries)
    }
}

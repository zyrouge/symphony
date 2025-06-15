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

    @RawQuery
    protected abstract fun findByNextIdRaw(query: SupportSQLiteQuery): Song.AlongSongQueueMapping?

    fun findByNextId(queueId: String, nextId: String): Song.AlongSongQueueMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_NEXT_ID} = ? " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId, nextId)
        return findByNextIdRaw(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findHeadRaw(query: SupportSQLiteQuery): Song.AlongSongQueueMapping?

    fun findHead(queueId: String): Song.AlongSongQueueMapping? {
        val query = "SELECT ${Song.TABLE}.*, " +
                "${SongQueueSongMapping.TABLE}.* " +
                "FROM ${SongQueueSongMapping.TABLE} " +
                "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? AND ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} = true " +
                "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID} "
        val args = arrayOf(queueId)
        return findHeadRaw(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [Song::class, SongQueueSongMapping::class])
    protected abstract fun entriesAsFlowRaw(query: SupportSQLiteQuery): Flow<
            Map<@MapColumn(SongQueueSongMapping.COLUMN_SONG_ID) String, Song.AlongSongQueueMapping>>

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
    fun transformEntriesAsValuesFlow(entries: Flow<Map<String, Song.AlongSongQueueMapping>>): Flow<List<Song>> {
        return entries.mapLatest {
            val list = mutableListOf<Song>()
            var head = it.firstNotNullOfOrNull {
                when {
                    it.value.mapping.isHead -> it.value
                    else -> null
                }
            }
            while (head != null) {
                list.add(head.entity)
                head = it[head.mapping.nextId]
            }
            list.toList()
        }
    }

    fun valuesAsFlow(queueId: String): Flow<List<Song>> {
        val entries = entriesAsFlow(queueId)
        return transformEntriesAsValuesFlow(entries)
    }
}

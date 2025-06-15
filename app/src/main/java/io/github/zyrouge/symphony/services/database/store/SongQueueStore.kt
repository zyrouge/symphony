package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongQueueStore {
    @Insert
    abstract suspend fun insert(vararg entities: SongQueue): List<String>

    @Update
    abstract suspend fun update(vararg entities: SongQueue): Int

    @RawQuery
    protected abstract suspend fun delete(query: SimpleSQLiteQuery): Int

    suspend fun delete(vararg ids: String): Int {
        val query = "DELETE FROM ${SongQueue.TABLE} WHERE ${SongQueue.COLUMN_ID} = :id"
        return delete(SimpleSQLiteQuery(query, ids))
    }

    @RawQuery
    protected abstract fun findByInternalId(query: SimpleSQLiteQuery): SongQueue.AlongAttributes?

    fun findByInternalId(internalId: Int): SongQueue.AlongAttributes? {
        val query = "SELECT * FROM ${SongQueue.TABLE} " +
                "WHERE ${SongQueue.COLUMN_INTERNAL_ID} = ?"
        val args = arrayOf(internalId)
        return findByInternalId(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [SongQueue::class, SongQueueSongMapping::class])
    protected abstract fun findFirstAsFlow(query: SupportSQLiteQuery): Flow<SongQueue.AlongAttributes?>

    fun findFirstAsFlow(): Flow<SongQueue.AlongAttributes?> {
        val query = "SELECT ${SongQueue.TABLE}.*, " +
                "COUNT(${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID}) as ${SongQueue.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
                "FROM ${SongQueue.TABLE} " +
                "LEFT JOIN ${SongQueueSongMapping.TABLE} ON ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ${SongQueue.TABLE}.${SongQueue.COLUMN_ID} " +
                "LIMIT 1"
        return findFirstAsFlow(SimpleSQLiteQuery(query))
    }

//    @RawQuery(observedEntities = [SongQueue::class, SongQueueSongMapping::class])
//    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<SongQueue.AlongAttributes>>

//fun SongQueueStore.valuesAsFlow(): Flow<List<SongQueue.AlongAttributes>> {
//    val query = "SELECT ${SongQueue.TABLE}.*, " +
//            "COUNT(${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID}) as ${SongQueue.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
//            "FROM ${SongQueue.TABLE} " +
//            "LEFT JOIN ${SongQueueSongMapping.TABLE} ON ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ${SongQueue.TABLE}.${SongQueue.COLUMN_ID}"
//    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
//}
}

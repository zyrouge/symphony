package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface SongQueueStore {
    @Insert
    suspend fun insert(vararg entities: SongQueue): List<String>

    @Query("DELETE FROM ${SongQueue.TABLE} WHERE ${SongQueue.COLUMN_ID} = :id")
    suspend fun delete(id: String): Int

    @RawQuery(observedEntities = [SongQueue::class, SongQueueSongMapping::class])
    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<SongQueue.AlongAttributes>>
}

fun SongQueueStore.valuesAsFlow(): Flow<List<SongQueue.AlongAttributes>> {
    val query = "SELECT ${SongQueue.TABLE}.*, " +
            "COUNT(${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_SONG_ID}) as ${SongQueue.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
            "FROM ${SongQueue.TABLE} " +
            "LEFT JOIN ${SongQueueSongMapping.TABLE} ON ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ${SongQueue.TABLE}.${SongQueue.COLUMN_ID}"
    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
}

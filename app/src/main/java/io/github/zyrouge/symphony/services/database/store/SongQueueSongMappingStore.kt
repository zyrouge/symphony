package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Dao
interface SongQueueSongMappingStore {
    @Insert
    suspend fun insert(vararg entities: SongQueueSongMapping)

    @Query("DELETE FROM ${SongQueueSongMapping.TABLE} WHERE ${SongQueueSongMapping.COLUMN_QUEUE_ID} IN (:ids)")
    suspend fun deleteSongQueueIds(ids: Collection<String>)

    @RawQuery(observedEntities = [Song::class, SongQueueSongMapping::class])
    fun entriesAsFlowRaw(query: SupportSQLiteQuery): Flow<Map<@MapColumn(SongQueueSongMapping.COLUMN_SONG_ID) String, Song.AlongSongQueueMapping>>
}

fun SongQueueSongMappingStore.entriesAsFlow(queueId: String): Flow<Map<String, Song.AlongSongQueueMapping>> {
    val query = "SELECT ${Song.TABLE}.*, " +
            "${SongQueueSongMapping.TABLE}.* " +
            "FROM ${SongQueueSongMapping.TABLE} " +
            "WHERE ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_QUEUE_ID} = ? " +
            "LEFT JOIN ${Song.TABLE} ON ${Song.TABLE}.${Song.COLUMN_ID} = ${SongQueueSongMapping.COLUMN_SONG_ID} " +
            "ORDER BY ${SongQueueSongMapping.TABLE}.${SongQueueSongMapping.COLUMN_IS_HEAD} DESC"
    val args = arrayOf(queueId)
    return entriesAsFlowRaw(SimpleSQLiteQuery(query, args))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun SongQueueSongMappingStore.transformEntriesAsValuesFlow(entries: Flow<Map<String, Song.AlongSongQueueMapping>>): Flow<List<Song>> {
    return entries.mapLatest {
        val list = mutableListOf<Song>()
        var head = it.firstNotNullOfOrNull {
            when {
                it.value.mapping.isStart -> it.value
                else -> null
            }
        }
        while (head != null) {
            list.add(head.song)
            head = it[head.mapping.nextId]
        }
        list.toList()
    }
}

fun SongQueueSongMappingStore.valuesAsFlow(queueId: String): Flow<List<Song>> {
    val entries = entriesAsFlow(queueId)
    return transformEntriesAsValuesFlow(entries)
}

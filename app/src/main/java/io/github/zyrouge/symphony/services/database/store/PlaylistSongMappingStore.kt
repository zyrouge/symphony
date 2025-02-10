package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Dao
interface PlaylistSongMappingStore {
    @Insert
    suspend fun insert(vararg entities: PlaylistSongMapping)

    @Query("DELETE FROM ${PlaylistSongMapping.TABLE} WHERE ${PlaylistSongMapping.COLUMN_PLAYLIST_ID} IN (:ids)")
    suspend fun deletePlaylistIds(ids: Collection<String>)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun PlaylistSongMappingStore.valuesMappedAsFlow(
    songStore: SongStore,
    id: String,
    sortBy: SongRepository.SortBy,
    sortReverse: Boolean,
): Flow<List<Song>> {
    val query = songStore.valuesAsFlowQuery(
        sortBy,
        sortReverse,
        additionalClauseBeforeJoins = "JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID} ",
        additionalArgsBeforeJoins = arrayOf(id),
    )
    val entries = songStore.entriesAsPlaylistSongMappedFlowRaw(query)
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

package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
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

    @RawQuery(observedEntities = [SongArtworkIndex::class, PlaylistSongMapping::class])
    fun findTop4SongArtworksAsFlowRaw(query: SimpleSQLiteQuery): Flow<List<SongArtworkIndex>>

    @RawQuery
    fun findSongIdsByPlaylistInternalIdAsFlowRaw(query: SimpleSQLiteQuery): Flow<List<String>>
}

@OptIn(ExperimentalCoroutinesApi::class)
fun PlaylistSongMappingStore.valuesMappedAsFlow(
    songStore: SongStore,
    id: String,
    sortBy: SongRepository.SortBy,
    sortReverse: Boolean,
): Flow<List<Song>> {
    val query = songStore.valuesQuery(
        sortBy,
        sortReverse,
        additionalClauseBeforeJoins = "JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} = ${Song.COLUMN_ID} ",
        additionalArgsBeforeJoins = arrayOf(id),
    )
    val entries = songStore.entriesAsPlaylistSongMappedAsFlowRaw(query)
    return entries.mapLatest { transformEntriesAsValues(it) }
}

fun PlaylistSongMappingStore.transformEntriesAsValues(entries: Map<String, Song.AlongPlaylistMapping>): List<Song> {
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

fun PlaylistSongMappingStore.findTop4SongArtworksAsFlow(playlistId: String): Flow<List<SongArtworkIndex>> {
    val query = "SELECT ${SongArtworkIndex.TABLE}.* " +
            "FROM ${SongArtworkIndex.TABLE} " +
            "LEFT JOIN ${PlaylistSongMapping.TABLE} ON ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} = ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_SONG_ID} " +
            "WHERE ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_FILE} != null " +
            "ORDER BY DESC " +
            "LIMIT 4"
    val args = arrayOf(playlistId)
    return findTop4SongArtworksAsFlowRaw(SimpleSQLiteQuery(query, args))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun PlaylistSongMappingStore.findSongIdsByPlaylistInternalIdAsFlow(playlistInternalId: Int): Flow<List<String>> {
    val query = "SELECT ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_SONG_ID} " +
            "FROM ${PlaylistSongMapping.TABLE} " +
            "LEFT JOIN ${Playlist.TABLE} ON ${Playlist.TABLE}.${Playlist.COLUMN_INTERNAL_ID} = ? AND ${PlaylistSongMapping.TABLE}.${PlaylistSongMapping.COLUMN_PLAYLIST_ID} = ${Playlist.TABLE}.${Playlist.COLUMN_ID} "
    val args = arrayOf(playlistInternalId)
    return findSongIdsByPlaylistInternalIdAsFlowRaw(SimpleSQLiteQuery(query, args))
}

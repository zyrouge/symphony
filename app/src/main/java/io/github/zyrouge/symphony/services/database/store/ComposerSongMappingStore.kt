package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.ComposerSongMapping
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface ComposerSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: ComposerSongMapping)

    @RawQuery(observedEntities = [SongArtworkIndex::class, ComposerSongMapping::class])
    fun findTop4SongArtworksAsFlowRaw(query: SimpleSQLiteQuery): Flow<List<SongArtworkIndex>>
}

fun ComposerSongMappingStore.valuesMappedAsFlow(
    songStore: SongStore,
    id: String,
    sortBy: SongRepository.SortBy,
    sortReverse: Boolean,
) = songStore.valuesAsFlow(
    sortBy,
    sortReverse,
    additionalClauseBeforeJoins = "JOIN ${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_COMPOSER_ID} = ? ",
    additionalArgsBeforeJoins = arrayOf(id),
)

fun ComposerSongMappingStore.findTop4SongArtworksAsFlow(composerId: String): Flow<List<SongArtworkIndex>> {
    val query = "SELECT ${SongArtworkIndex.TABLE}.* " +
            "FROM ${SongArtworkIndex.TABLE} " +
            "LEFT JOIN ${ComposerSongMapping.TABLE} ON ${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_COMPOSER_ID} = ? AND ${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_SONG_ID} = ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_SONG_ID} " +
            "WHERE ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_FILE} != null " +
            "ORDER BY DESC " +
            "LIMIT 4"
    val args = arrayOf(composerId)
    return findTop4SongArtworksAsFlowRaw(SimpleSQLiteQuery(query, args))
}

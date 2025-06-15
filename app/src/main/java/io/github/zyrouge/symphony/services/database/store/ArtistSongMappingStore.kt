package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ArtistSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(vararg entities: ArtistSongMapping)

    @RawQuery(observedEntities = [SongArtworkIndex::class, ArtistSongMapping::class])
    protected abstract fun findTop4SongArtworksAsFlow(query: SimpleSQLiteQuery): Flow<List<SongArtworkIndex>>

    fun findTop4SongArtworksAsFlow(artistId: String): Flow<List<SongArtworkIndex>> {
        val query = "SELECT ${SongArtworkIndex.TABLE}.* " +
                "FROM ${SongArtworkIndex.TABLE} " +
                "LEFT JOIN ${ArtistSongMapping.TABLE} ON ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_ARTIST_ID} = ? AND ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_SONG_ID} = ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_SONG_ID} " +
                "WHERE ${SongArtworkIndex.TABLE}.${SongArtworkIndex.COLUMN_FILE} != null " +
                "ORDER BY DESC " +
                "LIMIT 4"
        val args = arrayOf(artistId)
        return findTop4SongArtworksAsFlow(SimpleSQLiteQuery(query, args))
    }

    fun valuesMappedAsFlow(
        songStore: SongStore,
        id: String,
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
    ) = songStore.valuesAsFlow(
        sortBy,
        sortReverse,
        additionalClauseBeforeJoins = "JOIN ${ArtistSongMapping.TABLE}.${ArtistSongMapping.COLUMN_ARTIST_ID} = ? ",
        additionalArgsBeforeJoins = arrayOf(id),
    )
}

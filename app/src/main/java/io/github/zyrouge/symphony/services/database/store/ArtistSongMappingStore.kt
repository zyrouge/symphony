package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository

@Dao
interface ArtistSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: ArtistSongMapping)
}

fun ArtistSongMappingStore.valuesMappedAsFlow(
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

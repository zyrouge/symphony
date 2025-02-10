package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.GenreSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository

@Dao
interface GenreSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: GenreSongMapping)
}

fun GenreSongMappingStore.valuesMappedAsFlow(
    songStore: SongStore,
    id: String,
    sortBy: SongRepository.SortBy,
    sortReverse: Boolean,
) = songStore.valuesAsFlow(
    sortBy,
    sortReverse,
    additionalClauseBeforeJoins = "JOIN ${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_GENRE_ID} = ? ",
    additionalArgsBeforeJoins = arrayOf(id),
)

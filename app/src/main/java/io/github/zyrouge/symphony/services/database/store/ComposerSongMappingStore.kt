package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.ComposerSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository

@Dao
interface ComposerSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: ComposerSongMapping)
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

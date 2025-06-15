package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.AlbumComposerMapping
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository

@Dao
abstract class AlbumComposerMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(vararg entities: AlbumComposerMapping)

    fun valuesMappedAsFlow(
        songStore: SongStore,
        id: String,
        sortBy: SongRepository.SortBy,
        sortReverse: Boolean,
    ) = songStore.valuesAsFlow(
        sortBy,
        sortReverse,
        additionalClauseBeforeJoins = "JOIN ${AlbumComposerMapping.TABLE}.${AlbumComposerMapping.COLUMN_COMPOSER_ID} = ? ",
        additionalArgsBeforeJoins = arrayOf(id),
    )
}

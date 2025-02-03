package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository

@Dao
interface AlbumSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: AlbumSongMapping)
}

fun AlbumSongMappingStore.valuesMappedAsFlow(
    songStore: SongStore,
    id: String,
    sortBy: SongRepository.SortBy,
    sortReverse: Boolean,
) = songStore.valuesAsFlow(
    sortBy,
    sortReverse,
    additionalClauseBeforeJoins = "JOIN ${AlbumSongMapping.TABLE}.${AlbumSongMapping.COLUMN_ALBUM_ID} = ? ",
    additionalArgsBeforeJoins = arrayOf(id),
)

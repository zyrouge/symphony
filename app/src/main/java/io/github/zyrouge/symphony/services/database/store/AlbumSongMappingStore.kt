package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping

@Dao
interface AlbumSongMappingStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: AlbumSongMapping)
}

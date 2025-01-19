package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping

@Dao
interface AlbumSongMappingStore {
    @Insert
    suspend fun insert(vararg entities: AlbumSongMapping)
}

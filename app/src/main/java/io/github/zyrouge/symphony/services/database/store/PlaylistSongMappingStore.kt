package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Transaction
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping

@Dao
interface PlaylistSongMappingStore {
    @Insert
    suspend fun insert(vararg entities: PlaylistSongMapping)

    @Insert
    @Transaction
    suspend fun updateInTransaction(vararg entities: PlaylistSongMapping): Int
}

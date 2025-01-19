package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistStore {
    @Insert
    suspend fun insert(vararg entities: Playlist): List<String>

    @Update
    suspend fun update(vararg entities: Playlist): Int

    @Query("DELETE FROM ${Playlist.TABLE} WHERE ${Playlist.COLUMN_ID} = :id")
    suspend fun delete(id: String): Int

    @Query("SELECT * FROM ${Playlist.TABLE}")
    suspend fun values(): List<Playlist>

    @Query("SELECT * FROM ${Playlist.TABLE}")
    suspend fun valuesAsFlow(): Flow<List<Playlist>>
}

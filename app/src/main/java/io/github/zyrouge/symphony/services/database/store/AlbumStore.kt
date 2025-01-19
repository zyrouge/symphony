package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumStore {
    @Insert
    suspend fun insert(vararg entities: Album): List<String>

    @Update
    suspend fun update(vararg entities: Album): Int

    @Query("SELECT * FROM ${Album.TABLE}")
    suspend fun values(): List<Album>

    @Query("SELECT * FROM ${Album.TABLE}")
    suspend fun valuesAsFlow(): Flow<List<Album>>
}

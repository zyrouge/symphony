package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistStore {
    @Insert
    suspend fun insert(vararg entities: Artist): List<String>

    @Update
    suspend fun update(vararg entities: Artist): Int

    @Query("SELECT * FROM ${Artist.TABLE}")
    suspend fun values(): List<Artist>

    @Query("SELECT * FROM ${Artist.TABLE}")
    suspend fun valuesAsFlow(): Flow<List<Artist>>
}

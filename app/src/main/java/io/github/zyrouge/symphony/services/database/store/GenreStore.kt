package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Genre
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreStore {
    @Insert
    suspend fun insert(vararg entities: Genre): List<String>

    @Update
    suspend fun update(vararg entities: Genre): Int

    @Query("SELECT * FROM ${Genre.TABLE}")
    suspend fun values(): List<Genre>

    @Query("SELECT * FROM ${Genre.TABLE}")
    suspend fun valuesAsFlow(): Flow<List<Genre>>
}

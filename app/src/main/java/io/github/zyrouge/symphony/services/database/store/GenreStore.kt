package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.zyrouge.symphony.services.groove.entities.Genre

@Dao
interface GenreStore {
    @Insert
    suspend fun insert(vararg entities: Genre): List<String>

    @Query("SELECT * FROM ${Genre.TABLE} WHERE ${Genre.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(name: String): Genre?
}

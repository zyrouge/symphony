package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import io.github.zyrouge.symphony.services.groove.entities.Genre

@Dao
interface GenreStore {
    @Insert
    suspend fun insert(vararg entities: Genre): List<String>

    @Query("SELECT * FROM ${Genre.TABLE} WHERE ${Genre.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(name: String): Genre?

    @Query("SELECT ${Genre.COLUMN_ID}, ${Genre.COLUMN_NAME} FROM ${Genre.TABLE} WHERE ${Genre.COLUMN_NAME} in (:names)")
    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Genre.COLUMN_NAME) String,
            @MapColumn(Genre.COLUMN_ID) String>
}

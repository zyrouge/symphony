package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Album

@Dao
interface AlbumStore {
    @Insert
    suspend fun insert(vararg entities: Album): List<String>

    @Update
    suspend fun update(vararg entities: Album): Int

    @Query("SELECT * FROM ${Album.TABLE} WHERE ${Album.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(name: String): Album?
}

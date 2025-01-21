package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Song

@Dao
interface SongStore {
    @Insert()
    suspend fun insert(vararg entities: Song): List<String>

    @Update
    suspend fun update(vararg entities: Song): Int

    @Query("DELETE FROM ${Song.TABLE} WHERE ${Song.COLUMN_ID} = :id")
    suspend fun delete(id: String): Int

    @Query("DELETE FROM ${Song.TABLE} WHERE ${Song.COLUMN_ID} IN (:ids)")
    suspend fun delete(ids: Collection<String>): Int

    @Query("SELECT * FROM ${Song.TABLE} WHERE ${Song.COLUMN_PATH} = :path LIMIT 1")
    fun findByPath(path: String): Song?

    @Query("SELECT ${Song.COLUMN_ID} FROM ${Song.TABLE}")
    fun ids(): List<String>
}

package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeLyricFile

@Dao
interface MediaTreeLyricFileStore {
    @Insert
    suspend fun insert(vararg entities: MediaTreeLyricFile): List<String>

    @Update
    suspend fun update(vararg entities: MediaTreeLyricFile): Int

    @Query("SELECT id FROM ${MediaTreeLyricFile.TABLE} WHERE ${MediaTreeLyricFile.COLUMN_PARENT_ID} = :parentId")
    fun ids(parentId: String): List<String>

    @Query("SELECT * FROM ${MediaTreeLyricFile.TABLE} WHERE ${MediaTreeLyricFile.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeLyricFile.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(parentId: String, name: String): MediaTreeLyricFile?

    @Query("SELECT * FROM ${MediaTreeLyricFile.TABLE} WHERE ${MediaTreeLyricFile.COLUMN_PARENT_ID} = :parentId")
    fun entriesNameMapped(parentId: String?): Map<@MapColumn(MediaTreeLyricFile.COLUMN_NAME) String, MediaTreeLyricFile>
}

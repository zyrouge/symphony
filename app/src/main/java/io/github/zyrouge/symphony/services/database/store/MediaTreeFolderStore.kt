package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder

@Dao
interface MediaTreeFolderStore {
    @Insert()
    suspend fun insert(vararg entities: MediaTreeFolder): List<String>

    @Update()
    suspend fun update(vararg entities: MediaTreeFolder): Int

    @Query("SELECT id FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId")
    fun ids(parentId: String): List<String>

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_IS_HEAD} = 1 AND ${MediaTreeFolder.COLUMN_NAME} = :name")
    fun findHeadByName(name: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeFolder.COLUMN_NAME} = :name")
    fun findByName(parentId: String, name: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId")
    fun entriesNameMapped(parentId: String): Map<@MapColumn(MediaTreeFolder.COLUMN_NAME) String, MediaTreeFolder>
}

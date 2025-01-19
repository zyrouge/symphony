package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaTreeFolderStore {
    @Insert()
    suspend fun insert(vararg entities: MediaTreeFolder): List<String>

    @Update()
    suspend fun update(vararg entities: MediaTreeFolder): Int

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeFolder.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(parentId: String?, name: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_INTERNAL_NAME} = :internalName LIMIT 1")
    fun findByInternalName(internalName: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE}")
    fun values(): List<MediaTreeFolder>

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE}")
    fun valuesAsFlow(): Flow<List<MediaTreeFolder>>
}

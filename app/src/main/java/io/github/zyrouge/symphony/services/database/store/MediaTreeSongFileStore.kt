package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile

@Dao
interface MediaTreeSongFileStore {
    @Insert
    suspend fun insert(vararg entities: MediaTreeSongFile): List<String>

    @Update
    suspend fun update(vararg entities: MediaTreeSongFile): Int

    @Query("DELETE FROM ${MediaTreeSongFile.TABLE} WHERE ${MediaTreeSongFile.COLUMN_ID} IN (:ids)")
    suspend fun delete(ids: Collection<String>): Int

    @Query("SELECT id FROM ${MediaTreeSongFile.TABLE} WHERE ${MediaTreeSongFile.COLUMN_PARENT_ID} = :parentId")
    fun ids(parentId: String): List<String>

    @Query("SELECT * FROM ${MediaTreeSongFile.TABLE} WHERE $${MediaTreeSongFile.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeSongFile.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(parentId: String, name: String): MediaTreeSongFile?

    @Query("SELECT * FROM ${MediaTreeSongFile.TABLE} WHERE ${MediaTreeSongFile.COLUMN_PARENT_ID} = :parentId")
    fun entriesNameMapped(parentId: String?): Map<@MapColumn(MediaTreeSongFile.COLUMN_NAME) String, MediaTreeSongFile>
}

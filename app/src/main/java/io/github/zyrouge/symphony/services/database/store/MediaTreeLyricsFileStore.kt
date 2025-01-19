package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeLyricsFile
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaTreeLyricsFileStore {
    @Insert()
    fun insert(vararg entities: MediaTreeLyricsFile): List<String>

    @Update()
    fun update(vararg entities: MediaTreeLyricsFile): Int

    @Query("SELECT * FROM ${MediaTreeLyricsFile.TABLE} WHERE ${MediaTreeLyricsFile.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeLyricsFile.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(parentId: String, name: String): MediaTreeLyricsFile?

    @Query("SELECT * FROM ${MediaTreeLyricsFile.TABLE}")
    suspend fun values(): List<MediaTreeLyricsFile>

    @Query("SELECT * FROM ${MediaTreeLyricsFile.TABLE}")
    suspend fun valuesAsFlow(): Flow<List<MediaTreeLyricsFile>>
}

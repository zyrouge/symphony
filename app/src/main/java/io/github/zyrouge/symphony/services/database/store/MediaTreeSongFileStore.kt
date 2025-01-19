package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaTreeSongFileStore {
    @Insert
    fun insert(vararg entities: MediaTreeSongFile): List<String>

    @Update
    fun update(vararg entities: MediaTreeSongFile): Int

    @Query("SELECT * FROM ${MediaTreeSongFile.TABLE} WHERE ${MediaTreeSongFile.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeSongFile.COLUMN_NAME} = :name LIMIT 1")
    fun findByName(parentId: String, name: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeSongFile.TABLE}")
    suspend fun values(): List<MediaTreeSongFile>

    @Query("SELECT * FROM ${MediaTreeSongFile.TABLE}")
    suspend fun valuesAsFlow(): Flow<List<MediaTreeSongFile>>
}

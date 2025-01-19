package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.SongFile

@Dao
interface SongFileStore {
    @Insert()
    suspend fun insert(vararg entities: SongFile): List<String>

    @Update
    suspend fun update(vararg entities: SongFile): Int

    @Query("UPDATE ${SongFile.TABLE} SET ${SongFile.COLUMN_DATE_MODIFIED} = 0")
    suspend fun updateDateModifiedToZero(): Int

    @Query("DELETE FROM ${SongFile.TABLE} WHERE ${SongFile.COLUMN_ID} = :id")
    suspend fun delete(id: String): Int

    @Query("DELETE FROM ${SongFile.TABLE} WHERE ${SongFile.COLUMN_ID} IN (:ids)")
    suspend fun delete(ids: Collection<String>): Int

    @Query("SELECT * FROM ${SongFile.TABLE}")
    suspend fun entriesPathMapped(): Map<@MapColumn(SongFile.COLUMN_PATH) String, SongFile>
}

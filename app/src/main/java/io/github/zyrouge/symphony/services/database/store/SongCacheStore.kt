package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.Song

@Dao
interface SongCacheStore {
    @Insert()
    suspend fun insert(vararg song: Song): List<Long>

    @Update
    suspend fun update(vararg song: Song): Int

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun delete(songId: String): Int

    @Query("DELETE FROM songs WHERE id IN (:songIds)")
    suspend fun delete(songIds: Collection<String>): Int

    @Query("DELETE FROM songs")
    suspend fun clear(): Int

    @Query("SELECT * FROM songs")
    suspend fun entriesPathMapped(): Map<@MapColumn("path") String, Song>
}

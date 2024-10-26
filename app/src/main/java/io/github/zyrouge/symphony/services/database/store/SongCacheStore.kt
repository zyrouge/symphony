package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.Song

@Dao
interface SongCacheStore {
    @Insert
    suspend fun insert(vararg song: Song)

    @Update
    suspend fun update(vararg song: Song)

    @Query("DELETE FROM songs WHERE id IN (:songIds)")
    suspend fun delete(songIds: Collection<String>)

    @Query("DELETE FROM songs")
    suspend fun clear()

    @Query("SELECT * FROM songs")
    suspend fun entriesPathMapped(): Map<@MapColumn("path") String, Song>
}

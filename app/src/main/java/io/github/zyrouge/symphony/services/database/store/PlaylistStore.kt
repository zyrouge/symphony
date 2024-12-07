package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.Playlist

@Dao
interface PlaylistStore {
    @Insert
    suspend fun insert(vararg playlist: Playlist): List<Long>

    @Update
    suspend fun update(vararg playlist: Playlist): Int

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun delete(playlistId: String): Int

    @Query("SELECT * FROM playlists")
    suspend fun entries(): Map<@MapColumn("id") String, Playlist>
}

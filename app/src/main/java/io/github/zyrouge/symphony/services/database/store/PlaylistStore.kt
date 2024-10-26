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
    suspend fun insert(vararg playlist: Playlist)

    @Update
    suspend fun update(vararg playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id IN (:playlistIds)")
    suspend fun delete(playlistIds: Collection<String>)

    @Query("SELECT * FROM playlists")
    suspend fun entries(): Map<@MapColumn("id") String, Playlist>
}

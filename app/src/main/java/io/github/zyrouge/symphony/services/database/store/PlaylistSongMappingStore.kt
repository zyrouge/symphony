package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping

@Dao
interface PlaylistSongMappingStore {
    @Insert
    suspend fun insert(vararg entities: PlaylistSongMapping)

    @Query("DELETE FROM ${PlaylistSongMapping.TABLE} WHERE ${PlaylistSongMapping.COLUMN_PLAYLIST_ID} IN (:ids)")
    suspend fun deletePlaylistIds(ids: Collection<String>)
}

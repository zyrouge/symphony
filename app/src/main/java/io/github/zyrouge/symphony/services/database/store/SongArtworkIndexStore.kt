package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex

@Dao
interface SongArtworkIndexStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: SongArtworkIndex): List<String>

    @Query("SELECT * FROM ${SongArtworkIndex.TABLE} WHERE ${SongArtworkIndex.COLUMN_SONG_ID} = :songId LIMIT 1")
    fun findBySongId(songId: String): SongArtworkIndex?

    @Query("SELECT * FROM ${SongArtworkIndex.TABLE} WHERE ${SongArtworkIndex.COLUMN_SONG_ID} != null")
    fun entriesSongIdMapped(): Map<@MapColumn(SongArtworkIndex.COLUMN_SONG_ID) String, SongArtworkIndex>
}

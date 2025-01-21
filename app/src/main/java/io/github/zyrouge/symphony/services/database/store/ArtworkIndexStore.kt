package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.zyrouge.symphony.services.groove.entities.ArtworkIndex

@Dao
interface ArtworkIndexStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg entities: ArtworkIndex): List<String>

    @Query("SELECT * FROM ${ArtworkIndex.TABLE}")
    fun entriesSongIdMapped(): Map<@MapColumn(ArtworkIndex.COLUMN_SONG_ID) String, ArtworkIndex>
}

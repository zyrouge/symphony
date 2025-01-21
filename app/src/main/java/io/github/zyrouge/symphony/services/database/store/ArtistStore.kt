package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Artist

@Dao
interface ArtistStore {
    @Insert
    suspend fun insert(vararg entities: Artist): List<String>

    @Update
    suspend fun update(vararg entities: Artist): Int

    @Query("SELECT ${Artist.COLUMN_ID}, ${Artist.COLUMN_NAME} FROM ${Artist.TABLE} WHERE ${Artist.COLUMN_NAME} in (:names)")
    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Artist.COLUMN_NAME) String,
            @MapColumn(Artist.COLUMN_ID) String>
}

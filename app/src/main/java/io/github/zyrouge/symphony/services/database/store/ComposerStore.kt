package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import io.github.zyrouge.symphony.services.groove.entities.Composer

@Dao
interface ComposerStore {
    @Insert
    suspend fun insert(vararg entities: Composer): List<String>

    @Update
    suspend fun update(vararg entities: Composer): Int

    @Query("SELECT ${Composer.COLUMN_ID}, ${Composer.COLUMN_NAME} FROM ${Composer.TABLE} WHERE ${Composer.COLUMN_NAME} in (:names)")
    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Composer.COLUMN_NAME) String,
            @MapColumn(Composer.COLUMN_ID) String>
}

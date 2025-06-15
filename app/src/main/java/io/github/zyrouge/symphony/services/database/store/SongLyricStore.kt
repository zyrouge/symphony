package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.zyrouge.symphony.services.groove.entities.SongLyric

@Dao
abstract class SongLyricStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(vararg entities: SongLyric)
}

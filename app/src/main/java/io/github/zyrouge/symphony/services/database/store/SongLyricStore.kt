package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.SongLyric
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongLyricStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(vararg entities: SongLyric)

    @RawQuery(observedEntities = [SongLyric::class])
    protected abstract fun findBySongIdAsFlow(query: SimpleSQLiteQuery): Flow<SongLyric?>

    fun findBySongIdAsFlow(songFileId: String): Flow<SongLyric?> {
        val query = "SELECT * FROM ${SongLyric.TABLE} " +
                "WHERE ${SongLyric.COLUMN_SONG_FILE_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(songFileId)
        return findBySongIdAsFlow(SimpleSQLiteQuery(query, args))
    }
}

package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongArtworkIndexStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(vararg entities: SongArtworkIndex): List<String>

    @RawQuery
    protected abstract fun findBySongIdAsFlow(query: SimpleSQLiteQuery): Flow<SongArtworkIndex?>

    fun findBySongIdAsFlow(songId: String): Flow<SongArtworkIndex?> {
        val query = "SELECT * FROM ${SongArtworkIndex.TABLE} " +
                "WHERE ${SongArtworkIndex.COLUMN_SONG_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(songId)
        return findBySongIdAsFlow(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesSongIdMapped(query: SimpleSQLiteQuery): Map<
            @MapColumn(SongArtworkIndex.COLUMN_SONG_ID) String, SongArtworkIndex>

    fun entriesSongIdMapped(): Map<@MapColumn(SongArtworkIndex.COLUMN_SONG_ID) String, SongArtworkIndex> {
        val query = "SELECT * FROM ${SongArtworkIndex.TABLE} " +
                "WHERE ${SongArtworkIndex.COLUMN_SONG_ID} != null"
        return entriesSongIdMapped(SimpleSQLiteQuery(query))
    }
}

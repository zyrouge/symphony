package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile
import io.github.zyrouge.symphony.utils.builtin.sqlqph

@Dao
abstract class MediaTreeSongFileStore {
    @Insert
    abstract suspend fun insert(vararg entities: MediaTreeSongFile): List<String>

    @Update
    abstract suspend fun update(vararg entities: MediaTreeSongFile): Int

    @RawQuery
    protected abstract suspend fun delete(query: SimpleSQLiteQuery): Int

    suspend fun delete(vararg ids: String): Int {
        val query = "DELETE FROM ${MediaTreeSongFile.TABLE} " +
                "WHERE ${MediaTreeSongFile.COLUMN_ID} IN (${sqlqph(ids.size)})"
        return delete(SimpleSQLiteQuery(query, ids))
    }

    @RawQuery
    protected abstract fun ids(query: SimpleSQLiteQuery): List<String>

    fun ids(parentId: String): List<String> {
        val query =
            "SELECT id FROM ${MediaTreeSongFile.TABLE} WHERE ${MediaTreeSongFile.COLUMN_PARENT_ID} = :parentId"
        val args = arrayOf(parentId)
        return ids(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findByName(query: SimpleSQLiteQuery): MediaTreeSongFile?

    fun findByName(parentId: String, name: String): MediaTreeSongFile? {
        val query = "SELECT * FROM ${MediaTreeSongFile.TABLE} " +
                "WHERE ${MediaTreeSongFile.COLUMN_PARENT_ID} = ? " +
                "AND ${MediaTreeSongFile.COLUMN_NAME} = ? " +
                "LIMIT 1"
        val args = arrayOf(parentId, name)
        return findByName(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesNameMapped(query: SimpleSQLiteQuery): Map<
            @MapColumn(MediaTreeSongFile.COLUMN_NAME) String, MediaTreeSongFile>

    fun entriesNameMapped(parentId: String?): Map<@MapColumn(MediaTreeSongFile.COLUMN_NAME) String, MediaTreeSongFile> {
        val query = "SELECT * FROM ${MediaTreeSongFile.TABLE} " +
                "WHERE ${MediaTreeSongFile.COLUMN_PARENT_ID} = ?"
        val args = arrayOf(parentId)
        return entriesNameMapped(SimpleSQLiteQuery(query, args))
    }
}

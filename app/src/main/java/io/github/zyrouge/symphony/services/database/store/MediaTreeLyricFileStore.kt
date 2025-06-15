package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeLyricFile

@Dao
abstract class MediaTreeLyricFileStore {
    @Insert
    abstract suspend fun insert(vararg entities: MediaTreeLyricFile): List<String>

    @Update
    abstract suspend fun update(vararg entities: MediaTreeLyricFile): Int

    @RawQuery
    protected abstract fun ids(query: SimpleSQLiteQuery): List<String>

    fun ids(parentId: String): List<String> {
        val query = "SELECT ${MediaTreeLyricFile.COLUMN_ID} " +
                "FROM ${MediaTreeLyricFile.TABLE} " +
                "WHERE ${MediaTreeLyricFile.COLUMN_PARENT_ID} = ?"
        val args = arrayOf(parentId)
        return ids(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findByName(query: SimpleSQLiteQuery): MediaTreeLyricFile?

    fun findByName(parentId: String, name: String): MediaTreeLyricFile? {
        val query = "SELECT * FROM ${MediaTreeLyricFile.TABLE} " +
                "WHERE ${MediaTreeLyricFile.COLUMN_PARENT_ID} = ? " +
                "AND ${MediaTreeLyricFile.COLUMN_NAME} = ? " +
                "LIMIT 1"
        val args = arrayOf(parentId, name)
        return findByName(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesNameMapped(query: SimpleSQLiteQuery): Map<
            @MapColumn(MediaTreeLyricFile.COLUMN_NAME) String, MediaTreeLyricFile>

    fun entriesNameMapped(parentId: String?): Map<@MapColumn(MediaTreeLyricFile.COLUMN_NAME) String, MediaTreeLyricFile> {
        val query = "SELECT * FROM ${MediaTreeLyricFile.TABLE} " +
                "WHERE ${MediaTreeLyricFile.COLUMN_PARENT_ID} = ?"
        val args = arrayOf(parentId)
        return entriesNameMapped(SimpleSQLiteQuery(query, args))
    }
}

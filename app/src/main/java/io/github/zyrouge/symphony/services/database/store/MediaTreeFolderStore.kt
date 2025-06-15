package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile
import io.github.zyrouge.symphony.services.groove.repositories.MediaTreeRepository
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MediaTreeFolderStore {
    @Insert
    abstract suspend fun insert(vararg entities: MediaTreeFolder): List<String>

    @Update
    abstract suspend fun update(vararg entities: MediaTreeFolder): Int

    @RawQuery
    protected abstract fun ids(query: SimpleSQLiteQuery): List<String>

    fun ids(parentId: String): List<String> {
        val query = "SELECT ${MediaTreeFolder.TABLE}.${MediaTreeFolder.COLUMN_ID} " +
                "FROM ${MediaTreeFolder.TABLE} " +
                "WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = ?"
        val args = arrayOf(parentId)
        return ids(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findHeadByName(query: SimpleSQLiteQuery): MediaTreeFolder?

    fun findHeadByName(name: String): MediaTreeFolder? {
        val query = "SELECT ${MediaTreeFolder.TABLE}.* " +
                "FROM ${MediaTreeFolder.TABLE} " +
                "WHERE ${MediaTreeFolder.COLUMN_IS_HEAD} = 1 " +
                "AND ${MediaTreeFolder.COLUMN_NAME} = ?"
        val args = arrayOf(name)
        return findHeadByName(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun findByName(query: SimpleSQLiteQuery): MediaTreeFolder?

    fun findByName(parentId: String, name: String): MediaTreeFolder? {
        val query = "SELECT ${MediaTreeFolder.TABLE}.* " +
                "FROM ${MediaTreeFolder.TABLE} " +
                "WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = ? " +
                "AND ${MediaTreeFolder.COLUMN_NAME} = ?"
        val args = arrayOf(parentId, name)
        return findByName(SimpleSQLiteQuery(query, args))
    }

    @RawQuery
    protected abstract fun entriesNameMapped(query: SimpleSQLiteQuery): Map<
            @MapColumn(MediaTreeFolder.COLUMN_NAME) String, MediaTreeFolder>

    fun entriesNameMapped(parentId: String): Map<@MapColumn(MediaTreeFolder.COLUMN_NAME) String, MediaTreeFolder> {
        val query = "SELECT ${MediaTreeFolder.TABLE}.* " +
                "FROM ${MediaTreeFolder.TABLE} " +
                "WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = ?"
        val args = arrayOf(parentId)
        return entriesNameMapped(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [MediaTreeFolder::class, MediaTreeSongFile::class])
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<MediaTreeFolder.AlongAttributes>>

    fun valuesAsFlow(
        sortBy: MediaTreeRepository.SortBy,
        sortReverse: Boolean,
    ): Flow<List<MediaTreeFolder.AlongAttributes>> {
        val orderBy = when (sortBy) {
            MediaTreeRepository.SortBy.CUSTOM -> "${MediaTreeFolder.TABLE}.${MediaTreeFolder.COLUMN_ID}"
            MediaTreeRepository.SortBy.TITLE -> "${MediaTreeFolder.TABLE}.${MediaTreeFolder.COLUMN_NAME}"
            MediaTreeRepository.SortBy.TRACKS_COUNT -> MediaTreeFolder.AlongAttributes.EMBEDDED_TRACKS_COUNT
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val query = "SELECT ${MediaTreeFolder.TABLE}.*, " +
                "COUNT(${MediaTreeSongFile.TABLE}.${MediaTreeSongFile.COLUMN_ID}) as ${MediaTreeFolder.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
                "FROM ${MediaTreeFolder.TABLE} " +
                "LEFT JOIN ${MediaTreeSongFile.TABLE} ON ${MediaTreeSongFile.TABLE}.${MediaTreeSongFile.COLUMN_PARENT_ID} = ${MediaTreeFolder.TABLE}.${MediaTreeFolder.COLUMN_ID} " +
                "WHERE ${MediaTreeFolder.AlongAttributes.EMBEDDED_TRACKS_COUNT} > 0 " +
                "ORDER BY $orderBy $orderDirection"
        return valuesAsFlow(SimpleSQLiteQuery(query))
    }
}

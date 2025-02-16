package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile
import io.github.zyrouge.symphony.services.groove.repositories.MediaTreeRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaTreeFolderStore {
    @Insert
    suspend fun insert(vararg entities: MediaTreeFolder): List<String>

    @Update
    suspend fun update(vararg entities: MediaTreeFolder): Int

    @Query("SELECT id FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId")
    fun ids(parentId: String): List<String>

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_IS_HEAD} = 1 AND ${MediaTreeFolder.COLUMN_NAME} = :name")
    fun findHeadByName(name: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId AND ${MediaTreeFolder.COLUMN_NAME} = :name")
    fun findByName(parentId: String, name: String): MediaTreeFolder?

    @Query("SELECT * FROM ${MediaTreeFolder.TABLE} WHERE ${MediaTreeFolder.COLUMN_PARENT_ID} = :parentId")
    fun entriesNameMapped(parentId: String): Map<@MapColumn(MediaTreeFolder.COLUMN_NAME) String, MediaTreeFolder>

    @RawQuery(observedEntities = [MediaTreeFolder::class, MediaTreeSongFile::class])
    fun valuesAsFlowRaw(query: SupportSQLiteQuery): Flow<List<MediaTreeFolder.AlongAttributes>>
}

fun MediaTreeFolderStore.valuesAsFlow(
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
    return valuesAsFlowRaw(SimpleSQLiteQuery(query))
}

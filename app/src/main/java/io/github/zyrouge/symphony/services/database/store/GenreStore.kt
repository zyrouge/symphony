package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.Genre
import io.github.zyrouge.symphony.services.groove.entities.GenreSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.GenreRepository
import io.github.zyrouge.symphony.utils.builtin.sqlqph
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GenreStore {
    @Insert
    abstract suspend fun insert(vararg entities: Genre)

    @RawQuery(observedEntities = [Genre::class, GenreSongMapping::class])
    protected abstract fun findByIdAsFlow(query: SupportSQLiteQuery): Flow<Genre.AlongAttributes?>

    fun findByIdAsFlow(id: String): Flow<Genre.AlongAttributes?> {
        val query = "SELECT ${Genre.TABLE}.*, " +
                "COUNT(${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_SONG_ID}) as ${Genre.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
                "FROM ${Genre.TABLE} " +
                "LEFT JOIN ${GenreSongMapping.TABLE} ON ${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_GENRE_ID} = ${Genre.TABLE}.${Genre.COLUMN_ID} " +
                "LEFT JOIN ${GenreSongMapping.TABLE} ON ${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_GENRE_ID} = ${Genre.TABLE}.${Genre.COLUMN_ID} " +
                "WHERE ${Genre.COLUMN_ID} = ? " +
                "LIMIT 1"
        val args = arrayOf(id)
        return findByIdAsFlow(SimpleSQLiteQuery(query, args))
    }

    @RawQuery(observedEntities = [Genre::class, GenreSongMapping::class])
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<Genre.AlongAttributes>>

    fun valuesAsFlow(
        sortBy: GenreRepository.SortBy,
        sortReverse: Boolean,
        songId: String? = null,
    ): Flow<List<Genre.AlongAttributes>> {
        val orderBy = when (sortBy) {
            GenreRepository.SortBy.CUSTOM -> "${Genre.TABLE}.${Genre.COLUMN_ID}"
            GenreRepository.SortBy.GENRE -> "${Genre.TABLE}.${Genre.COLUMN_NAME}"
            GenreRepository.SortBy.TRACKS_COUNT -> Genre.AlongAttributes.EMBEDDED_TRACKS_COUNT
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val songMappingJoin = "" +
                (if (songId != null) "${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_SONG_ID} = ? AND " else "") +
                "${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_GENRE_ID} = ${Genre.TABLE}.${Genre.COLUMN_ID}"
        val query = "SELECT ${Genre.TABLE}.*, " +
                "COUNT(${GenreSongMapping.TABLE}.${GenreSongMapping.COLUMN_SONG_ID}) as ${Genre.AlongAttributes.EMBEDDED_TRACKS_COUNT} " +
                "FROM ${Genre.TABLE} " +
                "LEFT JOIN ${GenreSongMapping.TABLE} ON $songMappingJoin " +
                "ORDER BY $orderBy $orderDirection"
        val args = mutableListOf<Any>()
        songId?.let { args.add(it) }
        return valuesAsFlow(SimpleSQLiteQuery(query, args.toTypedArray()))
    }

    @RawQuery
    protected abstract fun entriesByNameNameIdMapped(query: SupportSQLiteQuery): Map<
            @MapColumn(Genre.COLUMN_NAME) String,
            @MapColumn(Genre.COLUMN_ID) String>

    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Genre.COLUMN_NAME) String,
            @MapColumn(Genre.COLUMN_ID) String> {
        val query = "SELECT ${Genre.COLUMN_ID}, ${Genre.COLUMN_NAME} " +
                "FROM ${Genre.TABLE} " +
                "WHERE ${Genre.COLUMN_NAME} in (${sqlqph(names.size)})"
        val args = arrayOf(*names.toTypedArray())
        return entriesByNameNameIdMapped(SimpleSQLiteQuery(query, args))
    }
}

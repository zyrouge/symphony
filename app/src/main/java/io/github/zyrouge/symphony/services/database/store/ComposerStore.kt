package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.zyrouge.symphony.services.groove.entities.AlbumComposerMapping
import io.github.zyrouge.symphony.services.groove.entities.Composer
import io.github.zyrouge.symphony.services.groove.entities.ComposerSongMapping
import io.github.zyrouge.symphony.services.groove.repositories.ComposerRepository
import io.github.zyrouge.symphony.utils.builtin.sqlqph
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ComposerStore {
    @Insert
    abstract suspend fun insert(vararg entities: Composer): List<String>

    @Update
    abstract suspend fun update(vararg entities: Composer): Int

    @RawQuery(observedEntities = [Composer::class, ComposerSongMapping::class, AlbumComposerMapping::class])
    protected abstract fun valuesAsFlow(query: SupportSQLiteQuery): Flow<List<Composer.AlongAttributes>>

    fun valuesAsFlow(
        sortBy: ComposerRepository.SortBy,
        sortReverse: Boolean,
    ): Flow<List<Composer.AlongAttributes>> {
        val orderBy = when (sortBy) {
            ComposerRepository.SortBy.CUSTOM -> "${Composer.TABLE}.${Composer.COLUMN_ID}"
            ComposerRepository.SortBy.COMPOSER_NAME -> "${Composer.TABLE}.${Composer.COLUMN_NAME}"
            ComposerRepository.SortBy.TRACKS_COUNT -> Composer.AlongAttributes.EMBEDDED_TRACKS_COUNT
            ComposerRepository.SortBy.ALBUMS_COUNT -> Composer.AlongAttributes.EMBEDDED_ALBUMS_COUNT
        }
        val orderDirection = if (sortReverse) "DESC" else "ASC"
        val query = "SELECT ${Composer.TABLE}.*, " +
                "COUNT(${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_SONG_ID}) as ${Composer.AlongAttributes.EMBEDDED_TRACKS_COUNT}, " +
                "COUNT(${AlbumComposerMapping.TABLE}.${AlbumComposerMapping.COLUMN_ALBUM_ID}) as ${Composer.AlongAttributes.EMBEDDED_ALBUMS_COUNT} " +
                "FROM ${Composer.TABLE} " +
                "LEFT JOIN ${ComposerSongMapping.TABLE} ON ${ComposerSongMapping.TABLE}.${ComposerSongMapping.COLUMN_COMPOSER_ID} = ${Composer.TABLE}.${Composer.COLUMN_ID} " +
                "LEFT JOIN ${AlbumComposerMapping.TABLE} ON ${AlbumComposerMapping.TABLE}.${AlbumComposerMapping.COLUMN_COMPOSER_ID} = ${Composer.TABLE}.${Composer.COLUMN_ID}" +
                "ORDER BY $orderBy $orderDirection"
        return valuesAsFlow(SimpleSQLiteQuery(query))
    }

    @RawQuery
    protected abstract fun entriesByNameNameIdMapped(query: SimpleSQLiteQuery): Map<
            @MapColumn(Composer.COLUMN_NAME) String,
            @MapColumn(Composer.COLUMN_ID) String>

    fun entriesByNameNameIdMapped(names: Collection<String>): Map<
            @MapColumn(Composer.COLUMN_NAME) String,
            @MapColumn(Composer.COLUMN_ID) String> {
        val query = "SELECT ${Composer.COLUMN_ID}, ${Composer.COLUMN_NAME} " +
                "FROM ${Composer.TABLE} " +
                "WHERE ${Composer.COLUMN_NAME} IN (${sqlqph(names.size)})"
        val args = arrayOf(*names.toTypedArray())
        return entriesByNameNameIdMapped(SimpleSQLiteQuery(query, args))
    }
}

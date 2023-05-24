package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

enum class SongSortBy {
    CUSTOM,
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    DATE_ADDED,
    DATE_MODIFIED,
    COMPOSER,
    ALBUM_ARTIST,
    YEAR,
    FILENAME,
    TRACK_NUMBER,
}

class SongRepository(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<Long, Song>()
    internal val pathCache = ConcurrentHashMap<String, Long>()
    private val searcher = FuzzySearcher<Long>(
        options = listOf(
            FuzzySearchOption({ get(it)?.title }, 3),
            FuzzySearchOption({ get(it)?.filename }, 2),
            FuzzySearchOption({ get(it)?.artistName }),
            FuzzySearchOption({ get(it)?.albumName })
        )
    )

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<Long>>(listOf())
    val all = _all.asStateFlow()
    private val _id = MutableStateFlow(System.currentTimeMillis())
    val id = _id.asStateFlow()
    var explorer = MediaStoreExposer.createExplorer()

    private fun emitAll() {
        _all.tryEmit(ids())
        _id.tryEmit(System.currentTimeMillis())
    }

    internal fun onSong(song: Song) {
        cache[song.id] = song
        pathCache[song.path] = song.id
        val entity = explorer
            .addRelativePath(GrooveExplorer.Path(song.path)) as GrooveExplorer.File
        entity.data = song.id
        emitAll()
    }

    fun reset() {
        cache.clear()
        pathCache.clear()
        explorer = MediaStoreExposer.createExplorer()
        emitAll()
    }

    fun search(songIds: List<Long>, terms: String, limit: Int? = 7) = searcher
        .search(terms, songIds)
        .subListNonStrict(limit ?: songIds.size)

    fun sort(songIds: List<Long>, by: SongSortBy, reversed: Boolean): List<Long> {
        val sorted = when (by) {
            SongSortBy.CUSTOM -> songIds.toList()
            SongSortBy.TITLE -> songIds.sortedBy { get(it)?.title }
            SongSortBy.ARTIST -> songIds.sortedBy { get(it)?.artistName }
            SongSortBy.ALBUM -> songIds.sortedBy { get(it)?.albumName }
            SongSortBy.DURATION -> songIds.sortedBy { get(it)?.duration }
            SongSortBy.DATE_ADDED -> songIds.sortedBy { get(it)?.dateAdded }
            SongSortBy.DATE_MODIFIED -> songIds.sortedBy { get(it)?.dateModified }
            SongSortBy.COMPOSER -> songIds.sortedBy { get(it)?.composer }
            SongSortBy.ALBUM_ARTIST -> songIds.sortedBy { get(it)?.additional?.albumArtist }
            SongSortBy.YEAR -> songIds.sortedBy { get(it)?.year }
            SongSortBy.FILENAME -> songIds.sortedBy { get(it)?.filename }
            SongSortBy.TRACK_NUMBER -> songIds.sortedBy { get(it)?.trackNumber }
        }
        return if (reversed) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: Long) = cache[id]
    fun get(ids: List<Long>) = ids.mapNotNull { get(it) }
}

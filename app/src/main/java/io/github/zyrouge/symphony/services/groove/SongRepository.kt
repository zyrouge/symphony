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
    val cache = ConcurrentHashMap<Long, Song>()
    val pathCache = ConcurrentHashMap<String, Long>()
    var explorer = MediaStoreExposer.createExplorer()

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<Long>>(listOf())
    val all = _all.asStateFlow()
    private val _id = MutableStateFlow(System.currentTimeMillis())
    val id = _id.asStateFlow()

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

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: Long) = cache[id]

    companion object {
        val searcher = FuzzySearcher<Song>(
            options = listOf(
                FuzzySearchOption({ it.title }, 3),
                FuzzySearchOption({ it.filename }, 2),
                FuzzySearchOption({ it.artistName }),
                FuzzySearchOption({ it.albumName })
            )
        )

        fun search(songs: List<Song>, terms: String, limit: Int? = 7) = searcher
            .search(terms, songs)
            .subListNonStrict(limit ?: songs.size)

        fun sort(songs: List<Song>, by: SongSortBy, reversed: Boolean): List<Song> {
            val sorted = when (by) {
                SongSortBy.CUSTOM -> songs.toList()
                SongSortBy.TITLE -> songs.sortedBy { it.title }
                SongSortBy.ARTIST -> songs.sortedBy { it.artistName }
                SongSortBy.ALBUM -> songs.sortedBy { it.albumName }
                SongSortBy.DURATION -> songs.sortedBy { it.duration }
                SongSortBy.DATE_ADDED -> songs.sortedBy { it.dateAdded }
                SongSortBy.DATE_MODIFIED -> songs.sortedBy { it.dateModified }
                SongSortBy.COMPOSER -> songs.sortedBy { it.composer }
                SongSortBy.ALBUM_ARTIST -> songs.sortedBy { it.additional.albumArtist }
                SongSortBy.YEAR -> songs.sortedBy { it.year }
                SongSortBy.FILENAME -> songs.sortedBy { it.filename }
                SongSortBy.TRACK_NUMBER -> songs.sortedBy { it.trackNumber }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}

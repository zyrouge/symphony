package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.joinToStringIfNotEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
            FuzzySearchOption({ v -> get(v)?.title?.let { compareString(it) } }, 3),
            FuzzySearchOption({ v -> get(v)?.filename?.let { compareString(it) } }, 2),
            FuzzySearchOption({ v -> get(v)?.artists?.let { compareCollection(it) } }),
            FuzzySearchOption({ v -> get(v)?.album?.let { compareString(it) } })
        )
    )

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<Long>>(emptyList())
    val all = _all.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()
    private val _id = MutableStateFlow(System.currentTimeMillis())
    val id = _id.asStateFlow()
    var explorer = GrooveExplorer.Folder()

    private fun emitCount() = _count.update { cache.size }

    private fun emitIds() = _id.update {
        System.currentTimeMillis()
    }

    internal fun onSong(song: Song) {
        cache[song.id] = song
        pathCache[song.path] = song.id
        val entity = explorer
            .addRelativePath(GrooveExplorer.Path(song.path)) as GrooveExplorer.File
        entity.data = song.id
        emitIds()
        _all.update {
            it + song.id
        }
        emitCount()
    }

    internal fun onFinish() {}

    fun reset() {
        cache.clear()
        pathCache.clear()
        explorer = GrooveExplorer.Folder()
        emitIds()
        _all.update {
            emptyList()
        }
        emitCount()
    }

    fun search(songIds: List<Long>, terms: String, limit: Int = 7) = searcher
        .search(terms, songIds, maxLength = limit)

    fun sort(songIds: List<Long>, by: SongSortBy, reverse: Boolean): List<Long> {
        val sorted = when (by) {
            SongSortBy.CUSTOM -> songIds
            SongSortBy.TITLE -> songIds.sortedBy { get(it)?.title }
            SongSortBy.ARTIST -> songIds.sortedBy { get(it)?.artists?.joinToStringIfNotEmpty() }
            SongSortBy.ALBUM -> songIds.sortedBy { get(it)?.album }
            SongSortBy.DURATION -> songIds.sortedBy { get(it)?.duration }
            SongSortBy.DATE_ADDED -> songIds.sortedBy { get(it)?.dateAdded }
            SongSortBy.DATE_MODIFIED -> songIds.sortedBy { get(it)?.dateModified }
            SongSortBy.COMPOSER -> songIds.sortedBy { get(it)?.composers?.joinToStringIfNotEmpty() }
            SongSortBy.ALBUM_ARTIST -> songIds.sortedBy { get(it)?.additional?.albumArtists?.joinToStringIfNotEmpty() }
            SongSortBy.YEAR -> songIds.sortedBy { get(it)?.year }
            SongSortBy.FILENAME -> songIds.sortedBy { get(it)?.filename }
            SongSortBy.TRACK_NUMBER -> songIds.sortedBy { get(it)?.trackNumber }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: Long) = cache[id]
    fun get(ids: List<Long>) = ids.mapNotNull { get(it) }

    fun getArtworkUri(songId: Long): Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        .buildUpon()
        .run {
            appendPath(songId.toString())
            appendPath("albumart")
            build()
        }

    fun getDefaultArtworkUri() = Assets.getPlaceholderUri(symphony)

    fun createArtworkImageRequest(songId: Long) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(songId),
        fallback = Assets.getPlaceholderId(symphony),
    )
}

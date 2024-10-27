package io.github.zyrouge.symphony.services.groove.repositories

import android.net.Uri
import androidx.core.net.toUri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.KeyGenerator
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimpleFileSystem
import io.github.zyrouge.symphony.utils.SimplePath
import io.github.zyrouge.symphony.utils.joinToStringIfNotEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

class SongRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        ARTIST,
        ALBUM,
        DURATION,
        DATE_MODIFIED,
        COMPOSER,
        ALBUM_ARTIST,
        YEAR,
        FILENAME,
        TRACK_NUMBER,
    }

    private val cache = ConcurrentHashMap<String, Song>()
    internal val pathCache = ConcurrentHashMap<String, String>()
    internal val idGenerator = KeyGenerator.TimeIncremental()
    private val searcher = FuzzySearcher<String>(
        options = listOf(
            FuzzySearchOption({ v -> get(v)?.title?.let { compareString(it) } }, 3),
            FuzzySearchOption({ v -> get(v)?.filename?.let { compareString(it) } }, 2),
            FuzzySearchOption({ v -> get(v)?.artists?.let { compareCollection(it) } }),
            FuzzySearchOption({ v -> get(v)?.album?.let { compareString(it) } })
        )
    )

    val isUpdating get() = symphony.groove.exposer.isUpdating
    private val _all = MutableStateFlow<List<String>>(emptyList())
    val all = _all.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()
    private val _id = MutableStateFlow(System.currentTimeMillis())
    val id = _id.asStateFlow()
    var explorer = SimpleFileSystem.Folder()

    private fun emitCount() = _count.update { cache.size }

    private fun emitIds() = _id.update {
        System.currentTimeMillis()
    }

    internal fun onSong(song: Song) {
        cache[song.id] = song
        pathCache[song.path] = song.id
        explorer.addChildFile(SimplePath(song.path)).data = song.id
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
        explorer = SimpleFileSystem.Folder()
        emitIds()
        _all.update {
            emptyList()
        }
        emitCount()
    }

    fun search(songIds: List<String>, terms: String, limit: Int = 7) = searcher
        .search(terms, songIds, maxLength = limit)

    fun sort(songIds: List<String>, by: SortBy, reverse: Boolean): List<String> {
        val sorted = when (by) {
            SortBy.CUSTOM -> songIds
            SortBy.TITLE -> songIds.sortedBy { get(it)?.title }
            SortBy.ARTIST -> songIds.sortedBy { get(it)?.artists?.joinToStringIfNotEmpty() }
            SortBy.ALBUM -> songIds.sortedBy { get(it)?.album }
            SortBy.DURATION -> songIds.sortedBy { get(it)?.duration }
            SortBy.DATE_MODIFIED -> songIds.sortedBy { get(it)?.dateModified }
            SortBy.COMPOSER -> songIds.sortedBy { get(it)?.composers?.joinToStringIfNotEmpty() }
            SortBy.ALBUM_ARTIST -> songIds.sortedBy { get(it)?.albumArtists?.joinToStringIfNotEmpty() }
            SortBy.YEAR -> songIds.sortedBy { get(it)?.year }
            SortBy.FILENAME -> songIds.sortedBy { get(it)?.filename }
            SortBy.TRACK_NUMBER -> songIds.sortedWith(
                compareBy({ get(it)?.discNumber }, { get(it)?.trackNumber }),
            )
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: String) = cache[id]
    fun get(ids: List<String>) = ids.mapNotNull { get(it) }

    fun getArtworkUri(songId: String): Uri = get(songId)?.coverFile
        ?.let { symphony.database.artworkCache.get(it) }?.toUri()
        ?: getDefaultArtworkUri()

    fun getDefaultArtworkUri() = Assets.getPlaceholderUri(symphony)

    fun createArtworkImageRequest(songId: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(songId),
        fallback = Assets.getPlaceholderId(symphony),
    )

    suspend fun getLyrics(song: Song): String? {
        try {
            val lrcPath = SimplePath(song.path).nameWithoutExtension + ".lrc"
            symphony.groove.exposer.uris[lrcPath]?.let { uri ->
                symphony.applicationContext.contentResolver.openInputStream(uri)?.use {
                    return String(it.readBytes())
                }
            }
            return symphony.database.lyricsCache.get(song.id)
        } catch (err: Exception) {
            Logger.error("LyricsRepository", "fetch lyrics failed", err)
        }
        return null
    }
}

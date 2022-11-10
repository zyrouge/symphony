package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.SettingsKeys
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

enum class SongSortBy {
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
}

class SongRepository(private val symphony: Symphony) {
    private val cached = mutableMapOf<Long, Song>()
    val onUpdate = Eventer<Int>()

    private val searcher = FuzzySearcher<Song>(
        options = listOf(
            FuzzySearchOption({ it.title }, 3),
            FuzzySearchOption({ it.filename }, 2),
            FuzzySearchOption({ it.artistName }),
            FuzzySearchOption({ it.albumName })
        )
    )

    init {
        symphony.settings.onChange.subscribe { event ->
            if (event == SettingsKeys.songs_filter_pattern) {
                fetch()
            }
        }
    }

    fun fetch() {
        runBlocking {
            withContext(Dispatchers.Default) {
                fetchSync()
            }
        }
    }

    private fun fetchSync(): Int {
        cached.clear()
        val cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )
        cursor?.let {
            val regex = symphony.settings.getSongsFilterPattern()
                ?.let { literal -> Regex(literal, RegexOption.IGNORE_CASE) }
            while (it.moveToNext()) {
                val song = Song.fromCursor(it)
                if (regex?.containsMatchIn(song.filename) != false) {
                    cached[song.id] = song
                }
            }
        }
        cursor?.close()
        val total = cached.size
        onUpdate.dispatch(total)
        return total
    }

    fun getAll() = cached.values.toList()
    fun getSongWithId(songId: Long) = cached[songId]
    fun hasSongWithId(songId: Long) = getSongWithId(songId) != null

    fun getSongsOfArtist(artistName: String) = getAll().filter {
        it.artistName == artistName
    }

    fun getSongsOfAlbum(albumId: Long) = getAll().filter {
        it.albumId == albumId
    }

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(songs: List<Song>, by: SongSortBy, reversed: Boolean): List<Song> {
            val sorted = when (by) {
                SongSortBy.TITLE -> songs.sortedBy { it.title }
                SongSortBy.ARTIST -> songs.sortedBy { it.artistName }
                SongSortBy.ALBUM -> songs.sortedBy { it.albumName }
                SongSortBy.DURATION -> songs.sortedBy { it.duration }
                SongSortBy.DATE_ADDED -> songs.sortedBy { it.dateAdded }
                SongSortBy.DATE_MODIFIED -> songs.sortedBy { it.dateModified }
                SongSortBy.COMPOSER -> songs.sortedBy { it.composer }
                SongSortBy.ALBUM_ARTIST -> songs.sortedBy { it.albumArtist }
                SongSortBy.YEAR -> songs.sortedBy { it.year }
                SongSortBy.FILENAME -> songs.sortedBy { it.filename }
            }
            return if (reversed) sorted.reversed() else sorted
        }

        fun getFiltered(songs: List<Song>, regex: Regex) =
            songs.filter { regex.containsMatchIn(it.filename) }
    }
}
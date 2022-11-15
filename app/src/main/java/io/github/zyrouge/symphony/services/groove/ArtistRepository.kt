package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

enum class ArtistSortBy {
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class ArtistRepository(private val symphony: Symphony) {
    private val cached = mutableMapOf<String, Artist>()
    val onUpdate = Eventer<Int>()

    private val searcher = FuzzySearcher<Artist>(
        options = listOf(
            FuzzySearchOption({ it.artistName })
        )
    )

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
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Artists.ARTIST + " ASC"
        )
        cursor?.let {
            while (it.moveToNext()) {
                val artist = Artist.fromCursor(it)
                cached[artist.artistName] = artist
            }
        }
        cursor?.close()
        val total = cached.size
        onUpdate.dispatch(total)
        return total
    }

    fun getArtistArtworkUri(artistName: String): Uri {
        val album = symphony.groove.album.getAlbumOfArtist(artistName)
        return album?.getArtworkUri(symphony)
            ?: symphony.groove.album.getDefaultAlbumArtworkUri()
    }

    fun getAll() = cached.values.toList()
    fun getArtistFromName(artistName: String) = cached[artistName]

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(artists: List<Artist>, by: ArtistSortBy, reversed: Boolean): List<Artist> {
            val sorted = when (by) {
                ArtistSortBy.ARTIST_NAME -> artists.sortedBy { it.artistName }
                ArtistSortBy.TRACKS_COUNT -> artists.sortedBy { it.numberOfTracks }
                ArtistSortBy.ALBUMS_COUNT -> artists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict
import kotlinx.coroutines.Dispatchers

enum class AlbumSortBy {
    ALBUM_NAME,
    ARTIST_NAME,
}

class AlbumRepository(private val symphony: Symphony) {
    private val cached = mutableMapOf<Long, Album>()
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<Album>(
        options = listOf(
            FuzzySearchOption({ it.albumName }, 3),
            FuzzySearchOption({ it.artistName })
        )
    )

    fun fetch() {
        symphony.launchInScope(Dispatchers.Default) {
            fetchSync()
        }
    }

    private fun fetchSync() {
        cached.clear()
        val cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Albums.ALBUM + " ASC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                val album = Album.fromCursor(it)
                cached[album.albumId] = album
            }
            onUpdate.dispatch(null)
        }
    }

    fun getDefaultAlbumArtworkUri() = Assets.getPlaceholderUri(symphony.applicationContext)
    fun getAlbumArtworkUri(albumId: Long) =
        getAlbumArtworkUriNullable(albumId) ?: getDefaultAlbumArtworkUri()

    fun getAlbumArtworkUriNullable(albumId: Long): Uri? {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId
        )
        return when {
            symphony.shorty.checkIfMediaStoreThumbnailExists(uri) -> uri
            else -> null
        }
    }

    fun getAll() = cached.values.toList()
    fun getAlbumWithId(albumId: Long) = cached[albumId]

    fun getAlbumOfArtist(artistName: String) = cached.values.find {
        it.artistName == artistName
    }

    fun getAlbumsOfArtist(artistName: String) = cached.values.filter {
        it.artistName == artistName
    }

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(songs: List<Album>, by: AlbumSortBy, reversed: Boolean): List<Album> {
            val sorted = when (by) {
                AlbumSortBy.ALBUM_NAME -> songs.sortedBy { it.albumName }
                AlbumSortBy.ARTIST_NAME -> songs.sortedBy { it.artistName }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}

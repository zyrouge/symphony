package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException

enum class AlbumSortBy {
    ALBUM_NAME,
    ARTIST_NAME,
}

class AlbumRepository(private val symphony: Symphony) {
    private val cached = mutableMapOf<Long, Album>()
    val onUpdate = Eventer<Int>()

    private val searcher = FuzzySearcher<Album>(
        options = listOf(
            FuzzySearchOption({ it.albumName }, 3),
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
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Albums.ALBUM + " ASC"
        );
        cursor?.let {
            while (it.moveToNext()) {
                val album = Album.fromCursor(it)
                cached[album.albumId] = album
            }
        }
        cursor?.close()
        val total = cached.size
        onUpdate.dispatch(total)
        return total
    }

    fun fetchAlbumArtwork(albumId: Long): Bitmap {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId
        )
        val context = symphony.applicationContext
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(500, 500), null)
            } else {
                val source =
                    ImageDecoder.createSource(context.contentResolver, uri)
                return ImageDecoder.decodeBitmap(source)
            }
        } catch (_: IOException) {
            return Assets.getPlaceholder(context)
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

    fun search(terms: String) = searcher.search(terms, getAll(), 30f)

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

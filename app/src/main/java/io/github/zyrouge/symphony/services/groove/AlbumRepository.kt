package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import io.github.zyrouge.symphony.Symphony
import java.util.stream.Stream

class AlbumRepository(private val symphony: Symphony) {
    lateinit var cached: MutableMap<Long, Album>
    val onUpdate = Stream.builder<Int>()!!

    init {
        fetch()
    }

    fun fetch(): Int {
        val cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Albums.ALBUM + " ASC"
        );
        val albums = mutableMapOf<Long, Album>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val album = Album.fromCursor(cursor)
                albums[album.albumId] = album
            }
        }
        cached = albums
        val total = albums.size
        onUpdate.add(total)
        return total
    }

    fun fetchAlbumArtwork(albumId: Long): Bitmap {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            symphony.applicationContext.contentResolver.loadThumbnail(uri, Size(500, 500), null)
        } else {
            val source = ImageDecoder.createSource(symphony.applicationContext.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source)
        }
    }
}

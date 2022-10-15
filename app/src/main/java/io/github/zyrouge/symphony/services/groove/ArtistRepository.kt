package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import java.util.stream.Stream

class ArtistRepository(private val symphony: Symphony) {
    lateinit var cached: MutableMap<String, Artist>
    val onUpdate = Stream.builder<Int>()!!

    init {
        fetch()
    }

    fun fetch(): Int {
        var cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Artists.ARTIST + " ASC"
        );
        var artists = mutableMapOf<String, Artist>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val artist = Artist.fromCursor(cursor)
                artists[artist.artistName] = artist
            }
        }
        cached = artists
        val total = artists.size
        onUpdate.add(total)
        return total
    }

    fun fetchArtistArtwork(artistName: String): Bitmap {
        val album = symphony.groove.album.cached.values.find { it.artistName == artistName }
        return album!!.getArtwork(symphony)
    }
}
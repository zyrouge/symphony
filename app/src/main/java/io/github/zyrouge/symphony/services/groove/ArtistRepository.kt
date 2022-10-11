package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import io.github.zyrouge.symphony.Symphony
import java.util.stream.Stream

class ArtistRepository {
    lateinit var cached: MutableMap<String, Artist>
    val onUpdate = Stream.builder<Int>()!!

    fun init() {
        fetch()
    }

    fun fetch(): Int {
        var cursor = Symphony.context.contentResolver.query(
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
        Log.i("artist", "total: ${cached.size} (${cursor?.count})")
        cached.forEach {
            Log.i("artist", it.value.toString())
        }
        val total = artists.size
        onUpdate.add(total)
        return total
    }

    fun fetchArtistArtwork(artistName: String): Bitmap {
        val album = Symphony.groove.album.cached.values.find { it.artistName == artistName }
        return album!!.getArtwork()
    }
}
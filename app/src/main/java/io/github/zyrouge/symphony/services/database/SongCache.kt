package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.FileDatabaseAdapter
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.getIntOrNull
import io.github.zyrouge.symphony.utils.getStringOrNull
import org.json.JSONObject
import java.nio.file.Paths

class SongCache(val symphony: Symphony) {
    data class Attributes(
        val lastModified: Long,
        val albumArtist: String?,
        val bitrate: Int?,
        val genre: String?,
    ) {
        fun toJSONObject() = JSONObject().apply {
            put(LAST_MODIFIED, lastModified)
            put(ALBUM_ARTIST, albumArtist)
            put(BITRATE, bitrate)
            put(GENRE, genre)
        }

        companion object {
            private const val LAST_MODIFIED = "0"
            private const val ALBUM_ARTIST = "1"
            private const val BITRATE = "2"
            private const val GENRE = "3"

            fun fromJSONObject(json: JSONObject) = json.run {
                Attributes(
                    lastModified = getLong(LAST_MODIFIED),
                    albumArtist = getStringOrNull(ALBUM_ARTIST),
                    bitrate = getIntOrNull(BITRATE),
                    genre = getStringOrNull(GENRE),
                )
            }

            fun fromSong(song: Song) = Attributes(
                lastModified = song.dateModified,
                albumArtist = song.additional.albumArtist,
                bitrate = song.additional.bitrate,
                genre = song.additional.genre,
            )
        }
    }

    private val adapter = FileDatabaseAdapter(
        Paths
            .get(symphony.applicationContext.cacheDir.absolutePath, "song_cache.json")
            .toFile()
    )

    fun read(): Map<Long, Attributes> {
        val content = adapter.read()
        val output = mutableMapOf<Long, Attributes>()
        val parsed = JSONObject(content)
        for (x in parsed.keys()) {
            output[x.toLong()] = Attributes.fromJSONObject(parsed.getJSONObject(x))
        }
        return output
    }

    fun update(value: Map<Long, Attributes>) {
        val json = JSONObject()
        value.forEach { (k, v) ->
            json.put(k.toString(), v.toJSONObject())
        }
        adapter.overwrite(json.toString())
    }
}

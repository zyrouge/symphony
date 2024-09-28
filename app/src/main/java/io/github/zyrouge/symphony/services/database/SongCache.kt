package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.PersistentCacheDatabaseAdapter
import io.github.zyrouge.symphony.services.groove.Song
import org.json.JSONObject

class SongCache(val symphony: Symphony) {
    private val adapter = PersistentCacheDatabaseAdapter(
        symphony.applicationContext,
        "songs",
        0,
        SongTransformer()
    )

    private val tagged = mutableSetOf<String>()

    fun get(key: String): Song? {
        tagged.add(key)
        return adapter.get(key)
    }

    fun put(key: String, value: Song) {
        tagged.add(key)
        adapter.put(key, value)
    }

    fun delete(key: String): Boolean {
        tagged.remove(key)
        return adapter.delete(key)
    }

    fun trim() {
        adapter.retain(tagged)
    }

    fun clear() {
        adapter.clear()
    }

    private class SongTransformer : PersistentCacheDatabaseAdapter.Transformer<Song>() {
        override fun serialize(data: Song) = data.toJSONObject().toString()
        override fun deserialize(data: String) = Song.fromJSONObject(JSONObject(data))
    }
}

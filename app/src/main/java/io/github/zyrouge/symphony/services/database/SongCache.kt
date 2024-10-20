package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.SQLiteKeyValueDatabaseAdapter
import io.github.zyrouge.symphony.services.groove.Song

class SongCache(val symphony: Symphony) {
    private val adapter = SQLiteKeyValueDatabaseAdapter(
        SongTransformer(),
        SQLiteKeyValueDatabaseAdapter.CacheOpenHelper(symphony.applicationContext, "songs", 1)
    )

    fun get(key: String) = adapter.get(key)
    fun put(key: String, value: Song) = adapter.put(key, value)
    fun delete(key: String) = adapter.delete(key)
    fun delete(keys: Collection<String>) = adapter.delete(keys)
    fun keys() = adapter.keys()
    fun all() = adapter.all()
    fun clear() = adapter.clear()

    private class SongTransformer : SQLiteKeyValueDatabaseAdapter.Transformer<Song>() {
        override fun serialize(data: Song) = data.toJson()
        override fun deserialize(data: String) = Song.fromJson(data)
    }
}

package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.SQLiteKeyValueDatabaseAdapter
import io.github.zyrouge.symphony.services.groove.Playlist

class PlaylistsBox(private val symphony: Symphony) {
    private val adapter = SQLiteKeyValueDatabaseAdapter(
        PlaylistTransformer(),
        SQLiteKeyValueDatabaseAdapter.CacheOpenHelper(symphony.applicationContext, "songs", 1)
    )

    fun get(key: String) = adapter.get(key)
    fun put(key: String, value: Playlist) = adapter.put(key, value)
    fun delete(key: String) = adapter.delete(key)
    fun delete(keys: Collection<String>) = adapter.delete(keys)
    fun keys() = adapter.keys()
    fun all() = adapter.all()
    fun clear() = adapter.clear()

    private class PlaylistTransformer : SQLiteKeyValueDatabaseAdapter.Transformer<Playlist>() {
        override fun serialize(data: Playlist) = data.toJson()
        override fun deserialize(data: String) = Playlist.fromJson(data)
    }
}

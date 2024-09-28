package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.PersistentCacheDatabaseAdapter

class LyricsCache(val symphony: Symphony) {
    private val adapter = PersistentCacheDatabaseAdapter(
        symphony.applicationContext,
        "lyrics",
        0,
        PersistentCacheDatabaseAdapter.Transformer.AsString()
    )

    private val tagged = mutableSetOf<String>()

    fun get(key: String): String? {
        tagged.add(key)
        return adapter.get(key)
    }

    fun put(key: String, value: String) {
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
}

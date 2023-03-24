package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.FileDatabaseAdapter
import org.json.JSONObject
import java.nio.file.Paths

class LyricsCache(val symphony: Symphony) {
    private val adapter = FileDatabaseAdapter(
        Paths
            .get(symphony.applicationContext.cacheDir.absolutePath, "lyrics_cache.json")
            .toFile()
    )

    fun read(): Map<String, String> {
        val content = adapter.read()
        val output = mutableMapOf<String, String>()
        val parsed = JSONObject(content)
        for (x in parsed.keys()) {
            output[x] = parsed.getString(x)
        }
        return output
    }

    fun update(value: Map<String, String>) {
        val json = JSONObject(value)
        adapter.overwrite(json.toString())
    }
}

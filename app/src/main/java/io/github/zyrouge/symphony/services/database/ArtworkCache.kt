package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.FileTreeDatabaseAdapter
import java.nio.file.Paths

class ArtworkCache(val symphony: Symphony) {
    private val adapter = FileTreeDatabaseAdapter(
        Paths
            .get(symphony.applicationContext.dataDir.absolutePath, "covers")
            .toFile()
    )

    fun get(key: String) = adapter.get(key)
    fun all() = adapter.list()
    fun clear() = adapter.clear()
}

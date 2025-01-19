package io.github.zyrouge.symphony.services.database.store

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.FileTreeDatabaseAdapter
import java.nio.file.Paths

class ArtworkStore(val symphony: Symphony) {
    private val path = Paths.get(symphony.applicationContext.dataDir.absolutePath, "covers")
    private val adapter = FileTreeDatabaseAdapter(path.toFile())

    fun get(key: String) = adapter.get(key)
    fun all() = adapter.list()
}

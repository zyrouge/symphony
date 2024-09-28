package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.adapters.FileTreeDatabaseAdapter
import java.io.File
import java.nio.file.Paths

class ArtworkCache(val symphony: Symphony) {
    private val adapter = FileTreeDatabaseAdapter(
        Paths
            .get(symphony.applicationContext.dataDir.absolutePath, "covers")
            .toFile()
    )

    private val tagged = mutableSetOf<String>()

    fun get(key: String): File {
        tagged.add(key)
        return adapter.get(key)
    }

    fun delete(key: String): Boolean {
        tagged.remove(key)
        return get(key).delete()
    }

    fun trim() {
        val all = adapter.list()
        for (x in all) {
            if (tagged.contains(x.key)) {
                continue
            }
            x.value.delete()
        }
    }
}

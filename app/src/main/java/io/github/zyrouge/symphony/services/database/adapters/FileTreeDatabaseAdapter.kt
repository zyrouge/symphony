package io.github.zyrouge.symphony.services.database.adapters

import java.io.File

class FileTreeDatabaseAdapter(val tree: File) {
    fun get(name: String) = File(tree, name)

    fun list(): Map<String, File> {
        val all = mutableMapOf<String, File>()
        tree.list()?.let { names ->
            for (x in names) {
                all[x] = File(tree, x)
            }
        }
        return all
    }
}

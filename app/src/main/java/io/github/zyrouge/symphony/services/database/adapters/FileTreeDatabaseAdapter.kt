package io.github.zyrouge.symphony.services.database.adapters

import java.io.File

class FileTreeDatabaseAdapter(val tree: File) {
    init {
        tree.mkdirs()
    }

    fun get(name: String): File = File(tree, name)

    fun list(): List<String> = tree.list()?.toList() ?: emptyList()

    fun clear() {
        tree.deleteRecursively()
        tree.mkdirs()
    }
}

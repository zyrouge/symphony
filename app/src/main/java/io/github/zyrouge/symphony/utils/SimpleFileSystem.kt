package io.github.zyrouge.symphony.utils

import java.util.concurrent.ConcurrentHashMap

sealed class SimpleFileSystem(val parent: Folder?, val name: String) {
    val fullPath
        get(): SimplePath {
            val parts = mutableListOf(name)
            var currentParent = parent
            while (currentParent != null) {
                parts.add(0, currentParent.name)
                currentParent = currentParent.parent
            }
            return SimplePath(parts)
        }

    class File(
        parent: Folder? = null,
        name: String,
        var data: Any? = null,
    ) : SimpleFileSystem(parent, name)

    class Folder(
        parent: Folder? = null,
        name: String = "root",
        var children: ConcurrentHashMap<String, SimpleFileSystem> = ConcurrentHashMap(),
    ) : SimpleFileSystem(parent, name) {
        val isEmpty get() = children.isEmpty()
        val childFoldersCount get() = children.values.count { it is Folder }

        fun addChildFolder(name: String): Folder {
            if (children.containsKey(name)) {
                throw Exception("Child '$name' already exists")
            }
            val child = Folder(this, name)
            children[name] = child
            return child
        }

        fun addChildFile(name: String): File {
            if (children.containsKey(name)) {
                throw Exception("Child '$name' already exists")
            }
            val child = File(this, name)
            children[name] = child
            return child
        }

        private fun removeChildFile(name: String) {
            if (!children.containsKey(name)) {
                throw Exception("Child '$name' doesn't exist")
            }
            children.remove(name)
        }

        fun addChildFile(path: SimplePath): File {
            val parts = path.parts.toMutableList()
            var parent = this
            while (parts.size > 1) {
                val x = parts.removeAt(0)
                val found = parent.children[x]
                parent = when (found) {
                    is Folder -> found
                    null -> parent.addChildFolder(x)
                    else -> throw Exception("Child '$x' is not a folder")
                }
            }
            return parent.addChildFile(parts[0])
        }

        fun removeChildFile(path: SimplePath) {
            val parts = path.parts.toMutableList()
            var parent = this
            while (parts.size > 1) {
                val x = parts.removeAt(0)
                val found = parent.children[x]
                parent = when (found) {
                    is Folder -> found
                    null -> parent.addChildFolder(x)
                    else -> throw Exception("Child '$x' is not a folder")
                }
            }
            parent.removeChildFile(parts[0])
        }
    }
}

package io.github.zyrouge.symphony.services.groove

import java.util.concurrent.ConcurrentHashMap

enum class PathSortBy {
    CUSTOM,
    NAME,
}

object GrooveExplorer {
    abstract class Entity(val basename: String, var parent: Folder? = null) {
        val pathParts: List<String>
            get() = parent?.let { it.pathParts + basename } ?: listOf(basename)

        val fullPath: String
            get() = pathParts.joinToString("/")

        abstract fun addRelativePath(path: Path): Entity
    }

    class File(basename: String, parent: Folder? = null, var data: Any? = null) :
        Entity(basename, parent) {
        override fun addRelativePath(path: Path): Nothing {
            throw Exception("Cannot add paths to file")
        }
    }

    class Folder(
        basename: String = "root",
        parent: Folder? = null,
        var children: ConcurrentHashMap<String, Entity> = ConcurrentHashMap(),
    ) : Entity(basename, parent) {
        val isEmpty: Boolean get() = children.isEmpty()

        fun addChild(child: Entity): Entity {
            child.parent = this
            children[child.basename] = child
            return child
        }

        override fun addRelativePath(path: Path): Entity {
            if (!path.hasChildParts) {
                val child = when {
                    path.isFile -> File(path.firstPart)
                    else -> Folder(path.firstPart)
                }
                return addChild(child)
            }
            val child = children[path.firstPart] ?: addChild(Folder(path.firstPart))
            return child.addRelativePath(path.shift())
        }
    }

    class Path(val parts: List<String>) {
        constructor(path: String) : this(intoParts(path))

        val isAbsolute: Boolean get() = parts.firstOrNull() == ""
        val isFile: Boolean get() = isFileRegex.containsMatchIn(basename)

        val hasChildParts: Boolean get() = parts.size > 1
        val firstPart: String get() = parts.first()
        val basename: String get() = parts.last()
        val dirname: Path get() = Path(parts.subList(0, parts.size - 1))

        fun shift() = Path(parts.subList(1, parts.size))

        fun resolve(to: Path): Path {
            if (to.isAbsolute) return to
            val a = parts.toMutableList()
            val b = to.parts.toMutableList()
            while (true) {
                when (b.firstOrNull()) {
                    "." -> b.removeFirst()
                    ".." -> {
                        b.removeFirst()
                        a.removeLast()
                    }

                    else -> break
                }
            }
            a.addAll(b)
            return Path(a)
        }

        override fun toString() = parts.joinToString("/")

        companion object {
            private val isFileRegex = Regex(""".+\..+""")

            fun isAbsolute(path: String) = path.startsWith("/")

            private fun intoParts(path: String) =
                path.split("/", "\\").filter { it.isNotBlank() }
        }
    }

    fun sort(paths: List<String>, by: PathSortBy, reverse: Boolean): List<String> {
        val sorted = when (by) {
            PathSortBy.CUSTOM -> paths
            PathSortBy.NAME -> paths.sorted()
        }
        return if (reverse) sorted.reversed() else sorted
    }
}

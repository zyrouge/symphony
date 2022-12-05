package io.github.zyrouge.symphony.services.groove

object GrooveExplorer {
    abstract class Entity(val basename: String, var parent: Folder? = null) {
        val fullPath: List<String>
            get() = parent?.let { it.fullPath + basename } ?: listOf(basename)

        abstract fun addRelativePath(path: Path): Entity
    }

    class File(basename: String, parent: Folder? = null, var data: Any? = null) :
        Entity(basename, parent) {
        override fun addRelativePath(path: Path): Nothing {
            throw Exception("Cannot add paths to file")
        }
    }

    class Folder(
        basename: String,
        parent: Folder? = null,
        var children: MutableMap<String, Entity> = mutableMapOf(),
    ) : Entity(basename, parent) {
        val isEmpty: Boolean get() = children.isEmpty()

        fun addChild(child: Entity): Entity {
            child.parent = this
            children[child.basename] = child
            return child
        }

        override fun addRelativePath(path: Path): Entity {
            if (!path.hasChildParts) {
                return addChild(if (path.isFile) File(path.firstPart) else Folder(path.firstPart))
            }
            val child = children[path.firstPart] ?: addChild(Folder(path.firstPart))
            return child.addRelativePath(path.shift())
        }
    }

    class Path(val parts: MutableList<String> = mutableListOf()) {
        constructor(path: String) : this(intoParts(path).toMutableList())

        val hasChildParts: Boolean get() = parts.size > 1
        val firstPart: String get() = parts.first()
        val basename: String get() = parts.last()
        val isFile: Boolean get() = isFileRegex.containsMatchIn(basename)

        fun shift() = Path(parts.subList(1, parts.size))

        companion object {
            private val isFileRegex = Regex(""".+\..+""")
            private val intoPartsRegex = Regex(""".\\|/""")

            fun intoParts(path: String) = path.split(intoPartsRegex).filter { it.isNotBlank() }
        }
    }
}

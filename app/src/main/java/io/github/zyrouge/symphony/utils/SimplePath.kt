package io.github.zyrouge.symphony.utils

class SimplePath(val parts: List<String>) {
    constructor(path: String) : this(n(p(path)))
    constructor(path: String, vararg subParts: String) : this(n(p(path) + subParts))
    constructor(path: SimplePath, vararg subParts: String) : this(n(path.parts + subParts))

    val name get() = parts.last()
    val nameWithoutExtension get() = name.substringBeforeLast(".")
    val extension get() = name.substringAfterLast(".", "")
    val parent get() = if (parts.size > 1) SimplePath(parts.subList(0, parts.size)) else null
    val size get() = parts.size
    val pathString get() = parts.joinToString("/")

    fun join(vararg nParts: String) = SimplePath(this, *nParts)

    override fun toString() = pathString

    companion object {
        private fun p(path: String) = path.split("/", "\\")

        private fun n(parts: List<String>): List<String> {
            val normalized = mutableListOf<String>()
            for (x in parts) {
                when {
                    x.isEmpty() -> {}
                    x == "." -> {}
                    x == ".." -> normalized.removeAt(parts.lastIndex)
                    else -> normalized.add(x)
                }
            }
            return parts
        }
    }
}

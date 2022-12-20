package io.github.zyrouge.symphony.services.parsers

data class M3UEntry(val index: Int, val info: String, val path: String)

data class M3U(val entries: List<M3UEntry>) {
    fun stringify(): String {
        val buffer = StringBuilder()
        buffer.append("#EXTM3U")
        entries.forEach {
            buffer.append("\n\n\n#EXTINF:${it.index},${it.info}\n${it.path}")
        }
        return buffer.toString()
    }

    companion object {
        private val entryRegex = Regex("""#EXTINF:(\d+),(.+?)\n(.+)""")

        fun parse(content: String): M3U {
            val entries = entryRegex.findAll(content).map {
                M3UEntry(
                    index = it.groupValues[1].toInt(),
                    info = it.groupValues[2],
                    path = it.groupValues[3],
                )
            }
            return M3U(entries.toList())
        }
    }
}

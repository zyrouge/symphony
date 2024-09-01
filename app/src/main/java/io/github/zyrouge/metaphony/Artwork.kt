package io.github.zyrouge.metaphony

data class Artwork(
    val format: Format,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Artwork
        if (format != other.format) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    enum class Format {
        Jpeg,
        Png,
        Gif,
        Unknown;

        companion object {
            fun fromMimeType(value: String) = when (value) {
                "image/jpeg", "image/jpg" -> Jpeg
                "image/png" -> Png
                "image/gif" -> Gif
                else -> Unknown
            }
        }
    }
}

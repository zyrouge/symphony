package io.github.zyrouge.metaphony

data class AudioArtwork(
    val format: Format,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioArtwork
        if (format != other.format) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    enum class Format(val extension: String, val mimeType: String) {
        Jpeg("jpg", "image/jpg"),
        Png("png", "image/png"),
        Gif("gif", "image/gif"),
        Unknown("", "");

        companion object {
            fun fromMimeType(value: String) = when (value) {
                Jpeg.mimeType, "image/jpeg" -> Jpeg
                Png.mimeType -> Png
                Gif.mimeType -> Gif
                else -> Unknown
            }
        }
    }
}

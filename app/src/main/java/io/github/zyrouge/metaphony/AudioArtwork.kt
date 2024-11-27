package io.github.zyrouge.metaphony

import java.util.Objects

data class AudioArtwork(
    val format: Format,
    val data: ByteArray,
) {
    override fun equals(other: Any?) =
        other is AudioArtwork && format != other.format && data.contentEquals(other.data)

    override fun hashCode() = Objects.hash(format, data)

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

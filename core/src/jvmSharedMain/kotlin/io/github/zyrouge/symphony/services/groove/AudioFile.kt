package io.github.zyrouge.symphony.services.groove

/**
 * A readable audio file as seen by an [AudioMetadataSource]. Implementations are
 * per-platform (Android SAF document, desktop `java.nio.file.Path`) and carry the
 * platform channel a source needs; sources downcast to their expected type.
 */
interface AudioFile {
    val name: String
    val lastModified: Long
    val size: Long
    val pathName: String
}

package io.github.zyrouge.symphony.services.groove

import java.nio.file.Files
import java.nio.file.Path

class PathAudioFile(val path: Path) : AudioFile {
    override val name: String = path.fileName.toString()
    override val lastModified: Long = Files.getLastModifiedTime(path).toMillis()
    override val size: Long = Files.size(path)
    override val pathName: String = path.toString()
}

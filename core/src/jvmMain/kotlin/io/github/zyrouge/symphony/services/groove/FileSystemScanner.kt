package io.github.zyrouge.symphony.services.groove

import java.nio.file.Files
import java.nio.file.Path

object FileSystemScanner {
    val audioExtensions: Set<String> = setOf(
        "flac", "mp3", "ogg", "oga", "opus", "m4a", "m4b", "mp4", "aac", "wav", "aiff", "aif", "wma",
    )

    fun scan(root: Path): List<PathAudioFile> =
        Files.walk(root).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .filter { it.extensionLower() in audioExtensions }
                .sorted()
                .map { PathAudioFile(it) }
                .toList()
        }

    private fun Path.extensionLower(): String =
        fileName.toString().substringAfterLast('.', "").lowercase()
}

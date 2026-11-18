package io.github.zyrouge.symphony.services.groove

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class FileSystemScannerTest {
    @TempDir
    lateinit var temp: Path

    private fun stage(relative: String, fixture: String): Path {
        val target = temp.resolve(relative)
        Files.createDirectories(target.parent)
        val stream = javaClass.classLoader.getResourceAsStream(fixture)
        assertNotNull(stream, "fixture $fixture missing from the test classpath")
        stream!!.use { input ->
            Files.newOutputStream(target).use { output -> input.copyTo(output) }
        }
        return target
    }

    @Test
    fun `scan finds audio files recursively and skips everything else`() {
        stage("audio.flac", "audio.flac")
        stage("nested/audio.mp3", "audio.mp3")
        stage("nested/deeper/audio.ogg", "audio.ogg")
        Files.writeString(temp.resolve("notes.txt"), "not audio")
        Files.writeString(temp.resolve("cover.png"), "not audio either")

        val found = FileSystemScanner.scan(temp)

        assertEquals(
            listOf("audio.flac", "nested/audio.mp3", "nested/deeper/audio.ogg"),
            found.map { temp.relativize(it.path).toString() },
        )
    }

    @Test
    fun `scan returns nothing for an empty directory`() {
        assertEquals(emptyList<PathAudioFile>(), FileSystemScanner.scan(temp))
    }

    @Test
    fun `scanned files carry their file facts`() {
        stage("audio.flac", "audio.flac")

        val scanned = FileSystemScanner.scan(temp).single()

        assertEquals("audio.flac", scanned.name)
        assertEquals(temp.resolve("audio.flac").toString(), scanned.pathName)
        assertEquals(Files.size(temp.resolve("audio.flac")), scanned.size)
        assertEquals(Files.getLastModifiedTime(temp.resolve("audio.flac")).toMillis(), scanned.lastModified)
    }
}

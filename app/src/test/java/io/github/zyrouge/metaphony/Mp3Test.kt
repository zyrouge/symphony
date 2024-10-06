package io.github.zyrouge.metaphony

import io.github.zyrouge.metaphony.mp3.Mp3
import org.junit.jupiter.api.Test

class Mp3Test {
    @Test
    fun parse() {
        val klassLoader = object {}.javaClass.classLoader!!
//        val stream = klassLoader.getResourceAsStream("audio-id3v2.4.mp3")!!
        val metadata = klassLoader
            .getResourceAsStream("5SOS - Ghost of You.mp3")!!
            .use { Mp3.read(it) }
        println(metadata.title)
        println(metadata.artists)
        println(metadata.album)
        println(metadata.genres)
        println(metadata.artworks.firstOrNull()?.format)
//        println(metadata.rawUint8Atoms)
//        println(metadata.rawPictureAtoms[0])
//        metadata.artworks.firstOrNull()?.let {
//            val ext = it.format.name.lowercase()
//            val file = File(
//                klass.getResource(".")!!.path,
//                "flac-gen-pic.$ext"
//            )
//            file.writeBytes(it.data)
//            println("done! ${file.path}")
//        }
    }
}

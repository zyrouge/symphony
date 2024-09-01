package io.github.zyrouge.metaphony

import org.junit.jupiter.api.Test

class FlacTest {
    @Test
    fun parse() {
        val klassLoader = object {}.javaClass.classLoader!!
        val stream = klassLoader.getResourceAsStream("audio.flac")!!
        val metadata = Flac.read(stream)
        stream.close()
        println(metadata)
//        metadata.artwork?.let {
//            val ext = it.mimeType.split("/")[1]
//            val file = File(
//                klass.getResource(".")!!.path,
//                "flac-gen-pic.$ext"
//            )
//            file.writeBytes(it.data)
//            println("done! ${file.path}")
//        }
    }
}

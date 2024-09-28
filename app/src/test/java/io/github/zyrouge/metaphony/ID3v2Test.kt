import io.github.zyrouge.metaphony.mp3.Mp3
import org.junit.jupiter.api.Test

class ID3v2Test {
    @Test
    fun parse() {
        parseID3v23()
        parseID3v24()
    }

    @Test
    fun parseID3v23() {
        val klassLoader = object {}.javaClass.classLoader!!
        val stream = klassLoader.getResourceAsStream("audio-id3v2.3.mp3")!!
        val metadata = Mp3.read(stream)
        stream.close()
        println(metadata.genres)
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

    @Test
    fun parseID3v24() {
        val klassLoader = object {}.javaClass.classLoader!!
        val stream = klassLoader.getResourceAsStream("audio-id3v2.4.mp3")!!
        val metadata = Mp3.read(stream)
        stream.close()
        println(metadata.genres)
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

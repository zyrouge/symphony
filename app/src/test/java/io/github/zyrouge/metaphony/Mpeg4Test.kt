import io.github.zyrouge.metaphony.mpeg4.Mpeg4
import org.junit.jupiter.api.Test

class Mpeg4Test {
    @Test
    fun parse() {
        val klassLoader = object {}.javaClass.classLoader!!
        val stream = klassLoader.getResourceAsStream("audio-empty-2.m4a")!!
        val metadata = Mpeg4.read(stream)
        stream.close()
        println(metadata.artists)
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
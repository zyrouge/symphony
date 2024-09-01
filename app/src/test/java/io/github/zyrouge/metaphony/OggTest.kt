import io.github.zyrouge.metaphony.Ogg
import org.junit.jupiter.api.Test

class OggTest {
    @Test
    fun parse() {
        val klassLoader = object {}.javaClass.classLoader!!
        val stream = klassLoader.getResourceAsStream("audio-empty.ogg")!!
        val metadata = Ogg.read(stream)
        stream.close()
        println(metadata.trackNumber)
        println(metadata.trackTotal)
//        println(metadata.artists)
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

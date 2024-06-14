import io.github.zyrouge.metaphony.Flac
import kotlin.test.Test

class FlacTest {
    @Test
    fun parse() {
        val klass = object {}.javaClass
        val stream = klass.getResourceAsStream("audio.flac")!!
        val metadata = Flac.read(stream)
        stream.close()
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

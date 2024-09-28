package io.github.zyrouge.metaphony.mpeg4

import io.github.zyrouge.metaphony.mpeg4.Mpeg4Atoms.readAtoms
import java.io.InputStream

object Mpeg4 {
    fun read(input: InputStream): Mpeg4Metadata {
        val builder = Mpeg4Metadata.Builder()
        builder.readAtoms(input)
        return builder.done()
    }
}

package io.github.zyrouge.metaphony

import java.io.InputStream

object Mp3 {
    fun read(input: InputStream) = readMp3(input)
}

private fun readMp3(input: InputStream): ID3v2Metadata {
    val builder = ID3v2Metadata.Builder()
    builder.readID3v2Metadata(input)
    return builder.done()
}

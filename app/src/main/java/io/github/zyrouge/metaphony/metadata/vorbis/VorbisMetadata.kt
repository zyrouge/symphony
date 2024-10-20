package io.github.zyrouge.metaphony.metadata.vorbis

import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.AudioMetadata
import io.github.zyrouge.metaphony.utils.xDateToLocalDateOrNull
import io.github.zyrouge.metaphony.utils.xIntAfterSlashOrNull
import io.github.zyrouge.metaphony.utils.xIntBeforeSlashOrIntOrNull
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadLEBuffer
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xSplitToPairOrNull
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class VorbisMetadata(
    val comments: MutableMap<String, Set<String>> = mutableMapOf(),
    val pictures: MutableList<AudioArtwork> = mutableListOf(),
) : AudioMetadata.Buildable {
    override fun title() = comment("title")
    override fun artists() = comments("artist")
    override fun album() = comment("album")
    override fun albumArtists() = comments("albumartist", "album_artist")
    override fun composer() = comments("composer", "performer", "artist")
    override fun genres() = comments("genre")
    override fun trackNumber() = comment("tracknumber")?.xIntBeforeSlashOrIntOrNull()
    override fun trackTotal() = comment("tracknumber")?.xIntAfterSlashOrNull()
        ?: comment("tracktotal")?.toIntOrNull()

    override fun discNumber() = comment("discnumber")?.xIntBeforeSlashOrIntOrNull()
    override fun discTotal() = comment("discnumber")?.xIntAfterSlashOrNull()
        ?: comment("disctotal")?.toIntOrNull()

    override fun date() = comment("date")?.xDateToLocalDateOrNull()
    override fun year() = comment("year")?.toInt() ?: date()?.year
    override fun comments() = comments("comment")
    override fun encoder() = comment("encoder")
    override fun lyrics() = comment("lyrics", "lyrics-xxx")
    override fun artworks() = pictures

    private fun comment(vararg names: String) = comments(*names).firstOrNull()
    private fun comments(vararg names: String) = commentsOrNull(*names) ?: setOf()
    private fun commentsOrNull(vararg names: String) = names.firstNotNullOfOrNull {
        comments[it]
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun VorbisMetadata.readVorbisComments(input: InputStream) {
    val vendorLen = input.xReadLEBuffer(4).getInt()
    input.xSkipBytes(vendorLen)
    val commentsLen = input.xReadLEBuffer(4).getInt()
    for (i in 0 until commentsLen) {
        val rawLen = input.xReadLEBuffer(4).getInt()
        val raw = input.xReadString(rawLen)
        val comment = raw.xSplitToPairOrNull("=") ?: throw Exception("Invalid vorbis comment")
        val (name, value) = comment.let { it.first.lowercase() to it.second }
        when (name) {
            "metadata_block_picture" -> {
                Base64.decode(value).inputStream().use { pictureStream ->
                    readVorbisPicture(pictureStream)
                }
            }

            else -> comments.compute(name) { _, existing ->
                existing?.let { it + value } ?: setOf(value)
            }
        }
    }
}

fun VorbisMetadata.readVorbisPicture(input: InputStream) {
    // skip picture type
    input.xSkipBytes(4)
    val mimeLen = input.xReadInt(4)
    val mime = input.xReadString(mimeLen)
    val format = AudioArtwork.Format.fromMimeType(mime)
    val descLen = input.xReadInt(4)
    input.xSkipBytes(descLen)
    // skip width, height, color depth, color used
    input.xSkipBytes(4)
    input.xSkipBytes(4)
    input.xSkipBytes(4)
    input.xSkipBytes(4)
    val imageLen = input.xReadInt(4)
    val image = input.xReadBytes(imageLen)
    val artwork = AudioArtwork(format = format, data = image)
    pictures.add(artwork)
}
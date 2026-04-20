package me.zyrouge.symphony.metaphony

import me.zyrouge.symphony.metaphony.AudioMetadata.Picture
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AudioMetadataParser private constructor() {
    // Tags keys can be found at https://taglib.org/api/p_propertymapping.html
    val tags = mutableMapOf<String, MutableList<String>>()
    val pictures = mutableListOf<Picture>()
    val audioProperties = mutableMapOf<String, Int>()

    fun putTag(key: String, value: String) {
        tags.compute(key) { _, it ->
            when (it) {
                null -> mutableListOf(value)
                else -> {
                    it.add(value)
                    it
                }
            }
        }
    }

    fun putPicture(pictureType: String, mimeType: String, data: ByteArray) {
        pictures.add(Picture(pictureType, mimeType, data))
    }

    fun putAudioProperty(key: String, value: Int) {
        audioProperties.put(key, value)
    }

    external fun readMetadata(filename: String, fd: Int): Boolean

    fun toMetadata(): AudioMetadata {
        val (discNumber, discTotal) = parseSlashedNumber(tags["DISCNUMBER"]?.firstOrNull() ?: "")
        val (trackNumber, trackTotal) = parseSlashedNumber(tags["TRACKNUMBER"]?.firstOrNull() ?: "")
        return AudioMetadata(
            title = tags["TITLE"]?.firstOrNull(),
            album = tags["ALBUM"]?.firstOrNull(),
            artists = tags["ARTIST"]?.toSet() ?: emptySet(),
            albumArtists = tags["ALBUMARTIST"]?.toSet() ?: emptySet(),
            composers = tags["COMPOSER"]?.toSet() ?: emptySet(),
            genres = tags["GENRE"]?.toSet() ?: emptySet(),
            discNumber = discNumber,
            discTotal = discTotal,
            trackNumber = trackNumber,
            trackTotal = trackTotal ?: tags["TRACKTOTAL"]?.firstOrNull()?.toIntOrNull(),
            date = tags["DATE"]?.firstOrNull()?.let { parseDate(it) } ?: tags["YEAR"]?.firstOrNull()
                ?.let { parseDate(it) },
            lyrics = tags["LYRICS"]?.firstOrNull(),
            encoding = tags["ENCODING"]?.firstOrNull(),
            bitrate = audioProperties["BITRATE"],
            lengthInSeconds = audioProperties["LENGTH_SECONDS"],
            sampleRate = audioProperties["SAMPLE_RATE"],
            channels = audioProperties["CHANNELS"],
            pictures = pictures,
        )
    }

    companion object {
        init {
            System.loadLibrary("metaphony")
        }

        fun parse(filename: String, fd: Int): AudioMetadata? {
            val parser = AudioMetadataParser()
            val success = parser.readMetadata(filename, fd)
            if (!success) {
                return null
            }
            return parser.toMetadata()
        }

        private fun parseSlashedNumber(text: String): Pair<Int?, Int?> {
            val split = text.split("/")
            if (split.size != 2) {
                return text.toIntOrNull() to null
            }
            return split[0].toIntOrNull() to split[1].toIntOrNull()
        }

        val DATE_NOW: LocalDate = LocalDate.now()
        val DATE_YEAR_MONTH_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        private fun parseDate(text: String): LocalDate? {
            runCatching {
                return Year.parse(text).atMonth(DATE_NOW.month).atDay(DATE_NOW.dayOfMonth)
            }
            runCatching {
                return YearMonth.parse(text).atDay(DATE_NOW.dayOfMonth)
            }
            runCatching {
                return LocalDate.parse(text, DATE_YEAR_MONTH_DATE)
            }
            return null
        }
    }
}

package io.github.zyrouge.metaphony.id3v2

internal object ID3v2Genres {
    private val id3v2Genres = listOf(
        "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
        "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B",
        "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
        "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
        "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
        "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
        "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
        "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic",
        "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk",
        "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta",
        "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American",
        "Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes", "Trailer",
        "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro",
        "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock",
        "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival",
        "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
        "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band",
        "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
        "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus",
        "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba",
        "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle",
        "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall",
        "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
        "Britpop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
        "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
        "Christian Rock ", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop",
        "Synthpop",
        "Christmas", "Art Rock", "Baroque", "Bhangra", "Big Beat", "Breakbeat",
        "Chillout", "Downtempo", "Dub", "EBM", "Eclectic", "Electro",
        "Electroclash", "Emo", "Experimental", "Garage", "Global", "IDM",
        "Illbient", "Industro-Goth", "Jam Band", "Krautrock", "Leftfield", "Lounge",
        "Math Rock", "New Romantic", "Nu-Breakz", "Post-Punk", "Post-Rock", "Psytrance",
        "Shoegaze", "Space Rock", "Trop Rock", "World Music", "Neoclassical", "Audiobook",
        "Audio Theatre", "Neue Deutsche Welle", "Podcast", "Indie Rock", "G-Funk", "Dubstep",
        "Garage Rock", "Psybient",
    )

    private val id3v2GenreRegex = Regex.fromLiteral("""\d+""")

    private fun parseIDv2Genre(value: String): Set<String> {
        if (value[0] == '(') {
            val matches = id3v2GenreRegex.findAll(value)
            return matches.mapNotNull { id3v2Genres.getOrNull(it.value.toInt()) }.toSet()
        }
        value.toIntOrNull()?.let {
            val genre = id3v2Genres.getOrNull(it)
            return setOfNotNull(genre)
        }
        return value.split(ID3v2Frames.NULL_CHARACTER).toSet()
    }

    fun parseIDv2Genre(values: Set<String>) = values.flatMap { parseIDv2Genre(it) }.toSet()
}
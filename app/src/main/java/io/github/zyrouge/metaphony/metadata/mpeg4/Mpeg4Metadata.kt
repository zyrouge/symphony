package io.github.zyrouge.metaphony.metadata.mpeg4

import io.github.zyrouge.metaphony.AudioArtwork
import io.github.zyrouge.metaphony.AudioMetadata
import io.github.zyrouge.metaphony.utils.xDateToLocalDateOrNull

data class Mpeg4Metadata(
    val stringAtoms: MutableMap<String, Set<String>> = mutableMapOf(),
    val uint8Atoms: MutableMap<String, Int> = mutableMapOf(),
    val pictureAtoms: MutableList<AudioArtwork> = mutableListOf(),
) : AudioMetadata.Buildable {
    override fun title() = stringAtom("title")
    override fun artists() = stringAtoms("artist")
    override fun album() = stringAtom("album")
    override fun albumArtists() = stringAtoms("album_artist")
    override fun composer() = stringAtoms("composer")
    override fun genres() = stringAtoms("genre")
    override fun trackNumber() = uint8Atom("track")
    override fun trackTotal() = uint8Atom("track_total")
    override fun discNumber() = uint8Atom("disc")
    override fun discTotal() = uint8Atom("disc_total")
    override fun lyrics() = stringAtom("lyrics")
    override fun comments() = stringAtoms("comment")
    override fun artworks() = pictureAtoms
    override fun date() = stringAtom("year")?.xDateToLocalDateOrNull()
    override fun year() = date()?.year
    override fun encoder() = stringAtom("encoder")

    private fun stringAtom(name: String) = stringAtoms[name]?.firstOrNull()
    private fun stringAtoms(name: String) = stringAtoms[name] ?: setOf()
    private fun uint8Atom(name: String) = uint8Atoms[name]
}


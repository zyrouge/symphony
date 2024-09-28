package io.github.zyrouge.metaphony.mpeg4

import io.github.zyrouge.metaphony.Artwork
import io.github.zyrouge.metaphony.Metadata
import io.github.zyrouge.metaphony.utils.xDateToLocalDate
import java.time.LocalDate

data class Mpeg4Metadata(
    internal val rawStringAtoms: Map<String, Set<String>>,
    internal val rawUint8Atoms: Map<String, Int>,
    internal val rawPictureAtoms: List<Artwork>,
) : Metadata {
    override val title: String? get() = stringAtomSingle("title")
    override val artists: Set<String> get() = stringAtomMultiple("artist")
    override val album: String? get() = stringAtomSingle("album")
    override val albumArtists: Set<String> get() = stringAtomMultiple("album_artist")
    override val composer: Set<String> get() = stringAtomMultiple("composer")
    override val genres: Set<String> get() = stringAtomMultiple("genre")
    override val year: Int? get() = date?.year
    override val trackNumber: Int? get() = uint8Atom("track")
    override val trackTotal: Int? get() = uint8Atom("track_total")
    override val discNumber: Int? get() = uint8Atom("disc")
    override val discTotal: Int? get() = uint8Atom("disc_total")
    override val lyrics: String? get() = stringAtomSingle("lyrics")
    override val comments: Set<String> get() = stringAtomMultiple("comment")
    override val artworks: List<Artwork> get() = rawPictureAtoms

    override val date: LocalDate?
        get() {
            val raw = stringAtomSingle("year") ?: return null
            if (raw.isEmpty()) return null
            return raw.xDateToLocalDate()
        }

    internal fun stringAtomSingle(name: String) = rawStringAtoms[name]?.firstOrNull()
    internal fun stringAtomMultiple(name: String) = rawStringAtoms[name] ?: setOf()
    internal fun uint8Atom(name: String) = rawUint8Atoms[name]

    internal data class Builder(
        val stringAtoms: MutableMap<String, Set<String>> = mutableMapOf(),
        val uint8Atoms: MutableMap<String, Int> = mutableMapOf(),
        val pictureAtoms: MutableList<Artwork> = mutableListOf(),
    ) {
        fun done() = Mpeg4Metadata(
            rawStringAtoms = stringAtoms,
            rawUint8Atoms = uint8Atoms,
            rawPictureAtoms = pictureAtoms,
        )
    }
}




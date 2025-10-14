package io.github.zyrouge.symphony.services.groove.repositories

import androidx.core.net.toUri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class SongRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        ARTIST,
        ALBUM,
        DURATION,
        DATE_MODIFIED,
        COMPOSER,
        ALBUM_ARTIST,
        YEAR,
        FILENAME,
        TRACK_NUMBER,
    }

    fun findArtistsOfIdAsFlow(id: String) = symphony.database.artists.valuesAsFlow(
        ArtistRepository.SortBy.ARTIST_NAME,
        false,
        songId = id,
    )

    fun findAlbumArtistsOfIdAsFlow(id: String) = symphony.database.artists.valuesAsFlow(
        ArtistRepository.SortBy.ARTIST_NAME,
        false,
        songId = id,
        onlyAlbumArtists = true,
    )

    fun findComposersOfIdAsFlow(id: String) = symphony.database.composers.valuesAsFlow(
        ComposerRepository.SortBy.COMPOSER_NAME,
        false,
        songId = id,
    )

    fun findAlbumsOfIdAsFlow(id: String) = symphony.database.albums.valuesAsFlow(
        AlbumRepository.SortBy.ALBUM_NAME,
        false,
        songId = id,
    )

    fun findGenresOfIdAsFlow(id: String) = symphony.database.genres.valuesAsFlow(
        GenreRepository.SortBy.GENRE,
        false,
        songId = id,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getArtworkUriAsFlow(id: String) =
        symphony.database.songArtworkIndices.findBySongIdAsFlow(id)
            .mapLatest { index -> index?.let { getArtworkUriFromIndex(it) } }

    fun getArtworkUriFromIndex(index: SongArtworkIndex) = index.file?.let {
        symphony.database.songArtworks.get(it).toUri()
    }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean, limit: Int? = null) =
        symphony.database.songs.valuesAsFlow(sortBy, sortReverse, limit = limit)
}

package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.findByIdAsFlow
import io.github.zyrouge.symphony.services.database.store.findTop4SongArtworksAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesMappedAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class ArtistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ARTIST_NAME,
        TRACKS_COUNT,
        ALBUMS_COUNT,
    }

    fun findByIdAsFlow(id: String) = symphony.database.artists.findByIdAsFlow(id)

    fun findAlbumsOfIdAsFlow(id: String) = symphony.database.albums.valuesAsFlow(
        AlbumRepository.SortBy.ARTIST_NAME,
        false,
        artistId = id,
    )

    fun findSongsByIdAsFlow(id: String, sortBy: SongRepository.SortBy, sortReverse: Boolean) =
        symphony.database.artistSongMapping.valuesMappedAsFlow(
            symphony.database.songs,
            id,
            sortBy,
            sortReverse
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTop4ArtworkUriAsFlow(id: String) =
        symphony.database.artistSongMapping.findTop4SongArtworksAsFlow(id)
            .mapLatest { indices ->
                indices.map { symphony.groove.song.getArtworkUriFromIndex(it) }
            }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean, onlyAlbumArtists: Boolean = false) =
        symphony.database.artists.valuesAsFlow(
            sortBy,
            sortReverse,
            onlyAlbumArtists = onlyAlbumArtists
        )
}

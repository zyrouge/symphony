package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.findByIdAsFlow
import io.github.zyrouge.symphony.services.database.store.findSongIdsByPlaylistInternalIdAsFlow
import io.github.zyrouge.symphony.services.database.store.findTop4SongArtworksAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesMappedAsFlow
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class PlaylistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        TRACKS_COUNT,
    }

    private val favoriteSongIdsFlow = symphony.database.playlistSongMapping
        .findSongIdsByPlaylistInternalIdAsFlow(PLAYLIST_INTERNAL_ID_FAVORITES)
    private var favoriteSongIds = emptyList<String>()

    init {
        symphony.groove.coroutineScope.launch {
            favoriteSongIdsFlow.collectLatest {
                favoriteSongIds = it
            }
        }
    }

    data class AddOptions(
        val playlist: Playlist,
        val songIds: List<String> = emptyList(),
        val songPaths: List<String> = emptyList(),
    )

    fun add(options: AddOptions) {
        val mappings = mutableListOf<PlaylistSongMapping>()
        var nextId: String? = null
        for (i in (options.songPaths.size - 1) downTo 0) {
            val mapping = PlaylistSongMapping(
                id = symphony.database.playlistSongMappingIdGenerator.next(),
                playlistId = options.playlist.id,
                songId = null,
                songPath = options.songPaths[i],
                isHead = i == 0,
                nextId = nextId,
            )
            mappings.add(mapping)
            nextId = mapping.id
        }
        symphony.groove.coroutineScope.launch {
            symphony.database.playlists.insert(options.playlist)
            symphony.database.playlistSongMapping.insert(*mappings.toTypedArray())
        }
    }

    fun removeSongs(playlistId: String, songIds: List<String>) {
        // TODO: implement this
    }

    fun isFavoriteSong(songId: String) = favoriteSongIds.contains(songId)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isFavoriteSongAsFlow(songId: String) = favoriteSongIdsFlow.mapLatest {
        it.contains(songId)
    }

    fun findByIdAsFlow(id: String) = symphony.database.playlists.findByIdAsFlow(id)

    fun findSongsByIdAsFlow(id: String, sortBy: SongRepository.SortBy, sortReverse: Boolean) =
        symphony.database.playlistSongMapping.valuesMappedAsFlow(
            symphony.database.songs,
            id,
            sortBy,
            sortReverse
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTop4ArtworkUriAsFlow(id: String) =
        symphony.database.playlistSongMapping.findTop4SongArtworksAsFlow(id)
            .mapLatest { indices ->
                indices.map { symphony.groove.song.getArtworkUriFromIndex(it) }
            }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.playlists.valuesAsFlow(sortBy, sortReverse)

    companion object {
        const val PLAYLIST_INTERNAL_ID_FAVORITES = 1
    }
}

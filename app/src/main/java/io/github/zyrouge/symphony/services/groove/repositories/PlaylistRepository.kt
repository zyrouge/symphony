package io.github.zyrouge.symphony.services.groove.repositories

import android.net.Uri
import androidx.room.withTransaction
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PersistentDatabase
import io.github.zyrouge.symphony.services.database.store.PlaylistSongMappingStore
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.utils.lazy_linked_list.LazyLinkedListOperator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class PlaylistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        TRACKS_COUNT,
    }

    private class PlaylistSongMappingOperatorEntityFunctions :
        LazyLinkedListOperator.EntityFunctions<String, PlaylistSongMapping> {
        override fun getEntityId(entity: PlaylistSongMapping) = entity.id
        override fun getEntityNextId(entity: PlaylistSongMapping) = entity.nextId
        override fun getEntityIsHead(entity: PlaylistSongMapping) = entity.isHead

        override fun updateEntityNextId(entity: PlaylistSongMapping, nNextId: String?) =
            entity.copy(nextId = nNextId)

        override fun updateEntityIsHead(entity: PlaylistSongMapping, nIsHead: Boolean) =
            entity.copy(isHead = nIsHead)
    }

    private class PlaylistSongMappingOperatorPersistenceFunctions(
        private val persistentDatabase: PersistentDatabase,
        private val store: PlaylistSongMappingStore,
        private val playlistId: String,
    ) :
        LazyLinkedListOperator.PersistenceFunctions<String, PlaylistSongMapping> {
        override fun getEntitiesByIds(ids: List<String>) = store.entriesByIds(playlistId, ids)
            .mapValues { it.value.mapping }

        override fun getEntitiesByNextIds(nextIds: List<String>) =
            store.entriesByNextIds(playlistId, nextIds)
                .mapValues { it.value.mapping }

        override fun getHeadEntity() = store.findHead(playlistId)?.mapping
        override fun getTailEntity() = store.findByNextId(playlistId, null)?.mapping

        override suspend fun saveEntities(
            addedEntities: List<PlaylistSongMapping>,
            modifiedEntities: List<PlaylistSongMapping>,
            deletedKeys: List<String>,
        ) {
            if (addedEntities.isEmpty() || modifiedEntities.isEmpty() || deletedKeys.isEmpty()) {
                return
            }
            persistentDatabase.withTransaction {
                if (addedEntities.isNotEmpty()) {
                    store.insert(*addedEntities.toTypedArray())
                }
                if (modifiedEntities.isNotEmpty()) {
                    store.update(*modifiedEntities.toTypedArray())
                }
                if (deletedKeys.isNotEmpty()) {
                    store.delete(playlistId, deletedKeys)
                }
            }
        }
    }

    private interface PlaylistSongMappingOperatorDataChangeFunctions :
        LazyLinkedListOperator.DataChangeFunctions<String, PlaylistSongMapping>

    private lateinit var favoritesPlaylistId: String
    private lateinit var favoriteSongIdsFlow: Flow<List<String>>
    private var favoriteSongIds = emptyList<String>()

    init {
        observeFavoritesPlaylistChanges()
    }

    suspend fun create(fn: (id: String) -> Playlist): Playlist {
        val playlist = fn(symphony.database.playlistsIdGenerator.next())
        symphony.database.playlists.insert(playlist)
        return playlist
    }

    suspend fun save(playlist: Playlist): Boolean {
        return symphony.database.playlists.update(playlist) > 0
    }

    sealed class AddPosition {
        object BeforeHead : AddPosition()
        class After(val id: String) : AddPosition()
        object AfterTail : AddPosition()
    }

    suspend fun addSongs(
        playlistId: String,
        songs: List<Song>,
        position: AddPosition = AddPosition.AfterTail,
    ) = addSongs(playlistId, songs.map { it.id }, position)

    suspend fun addSongs(
        playlistId: String,
        songIds: List<String>,
        position: AddPosition = AddPosition.AfterTail,
    ): Boolean {
        val operator = createPlaylistSongMappingOperator(playlistId)
        val changeset = when (position) {
            AddPosition.BeforeHead -> operator.prependHead(songIds) { x, isHead, nextId ->
                PlaylistSongMapping(
                    id = symphony.database.playlistSongMappingIdGenerator.next(),
                    playlistId = playlistId,
                    songId = x,
                    rawSongPath = null,
                    isHead = isHead,
                    nextId = nextId,
                )
            }

            is AddPosition.After, AddPosition.AfterTail -> {
                val insertAfterId = if (position is AddPosition.After) position.id else null
                operator.append(insertAfterId, songIds) { x, isHead, nextId ->
                    PlaylistSongMapping(
                        id = symphony.database.playlistSongMappingIdGenerator.next(),
                        playlistId = playlistId,
                        songId = x,
                        rawSongPath = null,
                        isHead = isHead,
                        nextId = nextId,
                    )
                }
            }
        }
        operator.persist(changeset)
        return changeset.addedKeys.isNotEmpty()
    }

    suspend fun removeSongs(playlistId: String, songs: List<Song>) =
        removeSongs(playlistId, songs.map { it.id })

    suspend fun removeSongs(playlistId: String, songIds: List<String>): Boolean {
        val operator = createPlaylistSongMappingOperator(playlistId)
        val changeset = operator.remove(songIds)
        operator.persist(changeset)
        return changeset.deletedKeys.isNotEmpty()
    }

    fun isFavoriteSong(songId: String) = favoriteSongIds.contains(songId)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isFavoriteSongAsFlow(songId: String) = favoriteSongIdsFlow.mapLatest {
        it.contains(songId)
    }

    suspend fun addToFavorites(songId: String) = addSongs(favoritesPlaylistId, listOf(songId))

    suspend fun removeFromFavorites(songId: String) =
        removeSongs(favoritesPlaylistId, listOf(songId))

    fun findByIdAsFlow(id: String) = symphony.database.playlists.findByIdAsFlow(id)

    fun findSongsById(id: String, sortBy: SongRepository.SortBy, sortReverse: Boolean) =
        symphony.database.playlistSongMapping.valuesMapped(
            symphony.database.songs,
            id,
            sortBy,
            sortReverse
        )

    fun findSongsByIdAsFlow(id: String, sortBy: SongRepository.SortBy, sortReverse: Boolean) =
        symphony.database.playlistSongMapping.valuesMappedAsFlow(
            symphony.database.songs,
            id,
            sortBy,
            sortReverse
        )

    fun findSongById(id: String, songId: String) =
        symphony.database.playlistSongMapping.findById(id, songId)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTop4ArtworkUriAsFlow(id: String) =
        symphony.database.playlistSongMapping.findTop4SongArtworksAsFlow(id)
            .mapLatest { indices ->
                indices.map { symphony.groove.song.getArtworkUriFromIndex(it) }
            }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.playlists.valuesAsFlow(sortBy, sortReverse)

    suspend fun export(playlist: Playlist, uri: Uri) {
        val songs = symphony.database.playlistSongMapping.valuesMapped(
            symphony.database.songs,
            playlist.id,
            SongRepository.SortBy.CUSTOM,
            false,
        )
        val outputStream = symphony.applicationContext.contentResolver.openOutputStream(uri, "w")
        outputStream?.use {
            val content = songs.joinToString("\n") { x -> x.path }
            it.write(content.toByteArray())
        }
    }

    private suspend fun ensureInternalPlaylist(internalId: Int, title: String): Playlist {
        symphony.database.playlists.findByInternalId(internalId)?.let {
            return it
        }
        return create { id ->
            Playlist(
                id = id,
                internalId = internalId,
                title = title,
                uri = null,
                path = null,
            )
        }
    }

    private suspend fun ensureFavoritesPlaylist() {
        val playlist = ensureInternalPlaylist(PLAYLIST_INTERNAL_ID_FAVORITES, symphony.t.Favorite)
        favoritesPlaylistId = playlist.id
    }

    private fun observeFavoritesPlaylistChanges() {
        symphony.groove.coroutineScope.launch {
            ensureFavoritesPlaylist()
            favoriteSongIdsFlow = symphony.database.playlistSongMapping
                .findSongIdsByPlaylistInternalIdAsFlow(PLAYLIST_INTERNAL_ID_FAVORITES)
            favoriteSongIdsFlow.collect {
                favoriteSongIds = it
            }
        }
    }

    private fun createPlaylistSongMappingOperator(
        playlistId: String,
        dataChangeFunctions: PlaylistSongMappingOperatorDataChangeFunctions? = null,
    ) = LazyLinkedListOperator(
        PlaylistSongMappingOperatorEntityFunctions(),
        PlaylistSongMappingOperatorPersistenceFunctions(
            symphony.database.persistent,
            symphony.database.playlistSongMapping,
            playlistId,
        ),
        dataChangeFunctions,
    )

    companion object {
        const val PLAYLIST_INTERNAL_ID_FAVORITES = 1
    }
}

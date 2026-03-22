package io.github.zyrouge.symphony.services.radio

import androidx.room.withTransaction
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.PersistentDatabase
import io.github.zyrouge.symphony.services.database.store.SongQueueSongMappingStore
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping
import io.github.zyrouge.symphony.utils.lazy_linked_list.LazyLinkedListOperator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class RadioQueue(private val symphony: Symphony) {
    private class SongQueueSongMappingOperatorEntityFunctions :
        LazyLinkedListOperator.EntityFunctions<String, SongQueueSongMapping> {
        override fun getEntityId(entity: SongQueueSongMapping) = entity.id
        override fun getEntityNextId(entity: SongQueueSongMapping) = entity.nextId
        override fun getEntityIsHead(entity: SongQueueSongMapping) = entity.isHead

        override fun updateEntityNextId(entity: SongQueueSongMapping, nNextId: String?) =
            entity.copy(nextId = nNextId)

        override fun updateEntityIsHead(entity: SongQueueSongMapping, nIsHead: Boolean) =
            entity.copy(isHead = nIsHead)
    }

    private class SongQueueSongMappingOperatorPersistenceFunctions(
        private val persistentDatabase: PersistentDatabase,
        private val store: SongQueueSongMappingStore,
        private val queueId: String,
    ) :
        LazyLinkedListOperator.PersistenceFunctions<String, SongQueueSongMapping> {
        override fun getEntitiesByIds(ids: List<String>) = store.entriesByIds(queueId, ids)
            .mapValues { it.value.mapping }

        override fun getEntitiesByNextIds(nextIds: List<String>) =
            store.entriesByNextIds(queueId, nextIds)
                .mapValues { it.value.mapping }

        override fun getHeadEntity() = store.findHead(queueId)?.mapping
        override fun getTailEntity() = store.findByNextId(queueId, null)?.mapping

        override suspend fun saveEntities(
            addedEntities: List<SongQueueSongMapping>,
            modifiedEntities: List<SongQueueSongMapping>,
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
                    store.delete(queueId, deletedKeys)
                }
            }
        }
    }

    private interface SongQueueSongMappingOperatorDataChangeFunctions :
        LazyLinkedListOperator.DataChangeFunctions<String, SongQueueSongMapping>

    init {
        observeSongQueueChanges()
    }

    sealed class AddPosition {
        object BeforeHead : AddPosition()
        class After(val id: String) : AddPosition()
        object AfterTail : AddPosition()
    }

    private suspend fun add(
        songQueue: SongQueue,
        songIds: List<String>,
        position: AddPosition = AddPosition.AfterTail,
    ): Boolean {
        val songQueueId = songQueue.id
        val operator = createSongQueueSongMappingOperator(songQueueId)
        val changeset = when (position) {
            AddPosition.BeforeHead -> operator.prependHead(songIds) { x, isHead, nextId ->
                SongQueueSongMapping(
                    id = symphony.database.songQueueSongMappingIdGenerator.next(),
                    queueId = songQueueId,
                    songId = x,
                    isHead = isHead,
                    nextId = nextId,
                )
            }

            is AddPosition.After, AddPosition.AfterTail -> {
                val insertAfterId = if (position is AddPosition.After) position.id else null
                operator.append(insertAfterId, songIds) { x, isHead, nextId ->
                    SongQueueSongMapping(
                        id = symphony.database.songQueueSongMappingIdGenerator.next(),
                        queueId = songQueueId,
                        songId = x,
                        isHead = isHead,
                        nextId = nextId,
                    )
                }
            }
        }
        operator.persist(changeset)
        songQueue.originalId?.let { originalSongQueueId ->
            val originalSongQueue =
                symphony.database.songQueue.findById(originalSongQueueId) ?: return@let
            // append at end for now
            add(originalSongQueue.entity, songIds, AddPosition.AfterTail)
        }
        return changeset.addedKeys.isNotEmpty()
    }

    suspend fun add(songIds: List<String>, position: AddPosition = AddPosition.AfterTail): Boolean {
        val songQueue = createOrGetCurrentSongQueue()
        return add(songQueue, songIds, position)
    }

    suspend fun add(songId: String, position: AddPosition = AddPosition.AfterTail) =
        add(listOf(songId), position)

    suspend fun add(songs: List<Song>, position: AddPosition = AddPosition.AfterTail) =
        add(songs.map { it.id }, position)

    suspend fun add(song: Song, position: AddPosition = AddPosition.AfterTail) =
        add(listOf(song.id), position)

    suspend fun remove(songQueue: SongQueue, songMappingIds: List<String>): Boolean {
        val songQueueId = songQueue.id
        val playingId = songQueue.playingId
        var nPlayingId = playingId
        val deletedSongIds = mutableListOf<String>()
        val operator = createSongQueueSongMappingOperator(
            songQueueId,
            object : SongQueueSongMappingOperatorDataChangeFunctions {
                override fun onMarkedAsDeleted(key: String, value: SongQueueSongMapping) {
                    if (key == nPlayingId) {
                        nPlayingId = value.nextId
                    }
                    value.songId?.let { deletedSongIds.add(it) }
                }
            }
        )
        val changeset = operator.remove(songMappingIds)
        if (playingId != nPlayingId) {
            val nSongQueue = songQueue.copy(playingId = nPlayingId)
            symphony.database.songQueue.update(nSongQueue)
        }
        operator.persist(changeset)
        songQueue.originalId?.let { originalSongQueueId ->
            val originalSongQueue =
                symphony.database.songQueue.findById(originalSongQueueId) ?: return@let
            val originalSongQueueSongMappingIds = symphony.database.songQueueSongMapping
                .entriesBySongIds(originalSongQueueId, deletedSongIds).values
                .map { it.mapping.id }
            remove(originalSongQueue.entity, originalSongQueueSongMappingIds)
        }
        return changeset.deletedKeys.isNotEmpty()
    }

    suspend fun remove(songMappingIds: List<String>): Boolean {
        val songQueue = getCurrentSongQueue() ?: return false
        return remove(songQueue.entity, songMappingIds)
    }

    suspend fun remove(songMappingId: String) = remove(listOf(songMappingId))

    suspend fun clear(): Boolean {
        val songQueue = getCurrentSongQueue() ?: return false
        symphony.database.songQueueSongMapping.deleteAll(songQueue.entity.id)
        return true
    }

    suspend fun toggleLoopMode() = updateCurrentSongQueue {
        val currentLoopMode = it.entity.loopMode
        val nextLoopModeOrdinal = (currentLoopMode.ordinal + 1) % SongQueue.LoopMode.values.size
        val nextLoopMode = SongQueue.LoopMode.values[nextLoopModeOrdinal]
        it.entity.copy(loopMode = nextLoopMode)
    }

    suspend fun setLoopMode(loopMode: SongQueue.LoopMode) = updateCurrentSongQueue {
        it.entity.copy(loopMode = loopMode)
    }

    suspend fun toggleShuffleMode(): Boolean {
        val songQueue = getCurrentSongQueue() ?: return false
        return setShuffleMode(songQueue.entity, !songQueue.entity.shuffled)
    }

    suspend fun setShuffleMode(to: Boolean): Boolean {
        val songQueue = getCurrentSongQueue() ?: return false
        return setShuffleMode(songQueue.entity, to)
    }

    private suspend fun setShuffleMode(songQueue: SongQueue, to: Boolean): Boolean {
        if (songQueue.shuffled == to) {
            return true
        }
        if (songQueue.shuffled) {
            return deleteShuffledQueue(songQueue)
        }
        return createShuffledQueue(songQueue)
    }

    private suspend fun createShuffledQueue(originalSongQueue: SongQueue): Boolean {
        val originalSongQueueId = originalSongQueue.id
        val nOriginalSongQueue = originalSongQueue.copy(internalId = null)
        val shuffledSongQueueId = symphony.database.songQueueIdGenerator.next()
        val songs = symphony.database.songQueueSongMapping.values(originalSongQueueId)
        val shuffledSongs = mutableListOf<SongQueueSongMapping>()
        var shuffledSongQueuePlayingId: String? = null
        var shuffledSongNextId: String? = null
        var i = 0
        for (x in songs.shuffled()) {
            val shuffledSongId = symphony.database.songQueueSongMappingIdGenerator.next()
            val shuffledSong = SongQueueSongMapping(
                id = shuffledSongId,
                queueId = shuffledSongQueueId,
                songId = x.mapping.songId,
                isHead = i == songs.size - 1,
                nextId = shuffledSongNextId,
            )
            shuffledSongs.add(shuffledSong)
            if (originalSongQueue.playingId == x.mapping.id) {
                shuffledSongQueuePlayingId = shuffledSongId
            }
            shuffledSongNextId = shuffledSongId
            i++
        }
        val shuffledSongQueue = originalSongQueue.copy(
            id = shuffledSongQueueId,
            originalId = originalSongQueueId,
            internalId = SONG_QUEUE_INTERNAL_ID_DEFAULT,
            playingId = shuffledSongQueuePlayingId,
            shuffled = true,
        )
        symphony.database.persistent.withTransaction {
            symphony.database.songQueue.update(nOriginalSongQueue)
            symphony.database.songQueue.insert(shuffledSongQueue)
            symphony.database.songQueueSongMapping.insert(*shuffledSongs.toTypedArray())
        }
        return true
    }

    private suspend fun deleteShuffledQueue(shuffledSongQueue: SongQueue): Boolean {
        val originalSongQueueId = shuffledSongQueue.originalId ?: return false
        val shuffledSongQueueId = shuffledSongQueue.id
        val nShuffledSongQueue = shuffledSongQueue.copy(internalId = null)
        val nOriginalSongQueuePlayingId = shuffledSongQueue.playingId
            ?.let { symphony.database.songQueueSongMapping.findById(shuffledSongQueueId, it) }
            ?.mapping?.songId
            ?.let { symphony.database.songQueueSongMapping.findBySongId(originalSongQueueId, it) }
            ?.mapping?.id
        val nOriginalSongQueue = shuffledSongQueue.copy(
            id = originalSongQueueId,
            internalId = SONG_QUEUE_INTERNAL_ID_DEFAULT,
            playingId = nOriginalSongQueuePlayingId,
            shuffled = false,
        )
        symphony.database.persistent.withTransaction {
            symphony.database.songQueue.update(nShuffledSongQueue)
            symphony.database.songQueue.insert(nOriginalSongQueue)
            symphony.database.songQueue.delete(shuffledSongQueueId)
        }
        return true
    }

    internal suspend fun updateCurrentSongQueue(fn: (SongQueue.AlongAttributes) -> SongQueue?): Boolean {
        val songQueue = getCurrentSongQueue() ?: return false
        val nSongQueue = fn(songQueue) ?: return false
        symphony.database.songQueue.update(nSongQueue)
        return true
    }

    internal fun getCurrentSongQueue() =
        symphony.database.songQueue.findByInternalId(SONG_QUEUE_INTERNAL_ID_DEFAULT)

    internal fun getCurrentSongQueueAsFlow() =
        symphony.database.songQueue.findByInternalIdAsFlow(SONG_QUEUE_INTERNAL_ID_DEFAULT)

    private suspend fun createOrGetCurrentSongQueue(): SongQueue {
        getCurrentSongQueue()?.let {
            return it.entity
        }
        val songQueue = SongQueue(
            id = symphony.database.songQueueIdGenerator.next(),
            internalId = SONG_QUEUE_INTERNAL_ID_DEFAULT,
            playingId = null,
            isPlaying = false,
            playingTimestamp = 0,
            playingTimestampUpdatedAt = 0,
            playingSpeedInt = SongQueue.SPEED_MULTIPLIER,
            playingPitchInt = SongQueue.PITCH_MULTIPLIER,
            shuffled = false,
            loopMode = SongQueue.LoopMode.None,
            speedInt = SongQueue.SPEED_MULTIPLIER,
            pitchInt = SongQueue.PITCH_MULTIPLIER,
            pauseOnSongEnd = false,
            sleepTimerEndsAt = null,
        )
        symphony.database.songQueue.insert(songQueue)
        return songQueue
    }

    private fun createSongQueueSongMappingOperator(
        queueId: String,
        dataChangeFunctions: SongQueueSongMappingOperatorDataChangeFunctions? = null,
    ) = LazyLinkedListOperator(
        SongQueueSongMappingOperatorEntityFunctions(),
        SongQueueSongMappingOperatorPersistenceFunctions(
            symphony.database.persistent,
            symphony.database.songQueueSongMapping,
            queueId,
        ),
        dataChangeFunctions,
    )

    private fun observeSongQueueChanges() {
        symphony.groove.coroutineScope.launch {
            observeSongQueueChangesNeedsSuspend()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeSongQueueChangesNeedsSuspend() {
        val songQueueFlow =
            symphony.database.songQueue.findByInternalIdAsFlow(SONG_QUEUE_INTERNAL_ID_DEFAULT)
                .distinctUntilChanged()
        val currentSongFlow = songQueueFlow
            .distinctUntilChangedBy { it?.entity?.playingId }
            .flatMapLatest {
                val queueId = it?.entity?.id
                val playingId = it?.entity?.playingId
                // ugly code since keeps throwing elvis incorrect warning
                if (queueId != null && playingId != null) {
                    symphony.database.songQueueSongMapping.findByIdAsFlow(queueId, playingId)
                } else {
                    emptyFlow()
                }
            }
        val nextSongFlow = currentSongFlow
            .distinctUntilChangedBy { it?.mapping?.nextId }
            .flatMapLatest {
                val queueId = it?.mapping?.queueId
                val nextSongMappingId = it?.mapping?.nextId
                if (queueId != null && nextSongMappingId != null) {
                    symphony.database.songQueueSongMapping.findByIdAsFlow(
                        queueId,
                        nextSongMappingId
                    )
                } else {
                    emptyFlow()
                }
            }
        currentSongFlow.collect {
            symphony.radio.onQueueCurrentPlayingSongChanged(it)
        }
        nextSongFlow.collect {
            symphony.radio.onQueueNextPlayingSongChanged(it)
        }
    }

    companion object {
        const val SONG_QUEUE_INTERNAL_ID_DEFAULT = 1
    }
}

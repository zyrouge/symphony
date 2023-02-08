package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.services.PermissionEvents
import kotlinx.coroutines.*

enum class GrooveKinds {
    SONG,
    ALBUM,
    ARTIST,
    ALBUM_ARTIST,
    GENRE,
    PLAYLIST,
}

class GrooveManager(private val symphony: Symphony) : SymphonyHooks {
    val coroutineScope = CoroutineScope(Dispatchers.Default)
    var readyDeferred = CompletableDeferred<Boolean>()

    val song = SongRepository(symphony)
    val album = AlbumRepository(symphony)
    val artist = ArtistRepository(symphony)
    val albumArtist = AlbumArtistRepository(symphony)
    val genre = GenreRepository(symphony)
    val playlist = PlaylistRepository(symphony)

    init {
        symphony.permission.onUpdate.subscribe {
            when (it) {
                PermissionEvents.MEDIA_PERMISSION_GRANTED -> coroutineScope.launch {
                    fetch()
                }
            }
        }
    }

    suspend fun fetch() {
        coroutineScope.launch {
            awaitAll(
                async { song.fetch() },
                async { album.fetch() },
                async { artist.fetch() },
            )
            playlist.fetch()
        }.join()
    }

    suspend fun reset() {
        coroutineScope.launch {
            awaitAll(
                async { song.reset() },
                async { album.reset() },
                async { artist.reset() },
                async { playlist.reset() },
            )
        }.join()
    }

    suspend fun refetch() {
        reset()
        fetch()
    }

    override fun onSymphonyReady() {
        coroutineScope.launch {
            fetch()
            readyDeferred.complete(true)
        }
    }
}

class GrooveRepositoryUpdateDispatcher(
    val maxCount: Int = 30,
    val minTimeDiff: Int = 200,
    val dispatch: () -> Unit,
) {
    var count = 0
    var time = currentTime

    fun increment() {
        if (count > maxCount && (currentTime - time) > minTimeDiff) {
            dispatch()
            count = 0
            time = System.currentTimeMillis()
            return
        }
        count++
    }

    companion object {
        private val currentTime: Long get() = System.currentTimeMillis()
    }
}

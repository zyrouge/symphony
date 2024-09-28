package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.services.PermissionEvents
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.dispatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

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

    val exposer = MediaExposer(symphony)
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

    internal fun onMediaStoreUpdate(value: Boolean) {
        playlist.onMediaStoreUpdate(value)
    }

    private suspend fun fetch() {
        coroutineScope.launch {
            exposer.fetch()
//            playlist.fetch()
        }.join()
    }

    private suspend fun reset() {
        coroutineScope.launch {
            awaitAll(
                async { exposer.reset() },
                async { albumArtist.reset() },
                async { album.reset() },
                async { artist.reset() },
                async { genre.reset() },
                async { playlist.reset() },
                async { song.reset() },
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

class GrooveEventerRapidUpdateDispatcher(
    val eventer: Eventer<Nothing?>,
    val maxCount: Int = 50,
    val minTimeDiff: Int = 250,
) {
    var count = 0
    var time = currentTime

    fun dispatch() {
        if (count > maxCount && (currentTime - time) > minTimeDiff) {
            eventer.dispatch()
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

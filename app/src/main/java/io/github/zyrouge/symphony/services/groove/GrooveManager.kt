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
}

class GrooveManager(private val symphony: Symphony) : SymphonyHooks {
    val coroutineScope = CoroutineScope(Dispatchers.Default)
    var readyDeferred = CompletableDeferred<Boolean>()

    val song = SongRepository(symphony)
    val album = AlbumRepository(symphony)
    val artist = ArtistRepository(symphony)
    val albumArtist = AlbumArtistRepository(symphony)
    val genre = GenreRepository(symphony)


    init {
        symphony.permission.onUpdate.subscribe {
            when (it) {
                PermissionEvents.MEDIA_PERMISSION_GRANTED -> fetch()
            }
        }
    }

    private fun fetch(postFetch: () -> Unit = {}) {
        coroutineScope.launch {
            awaitAll(
                async { song.fetch() },
                async { album.fetch() },
                async { artist.fetch() },
            )
            postFetch()
        }
    }

    override fun onSymphonyReady() {
        fetch {
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

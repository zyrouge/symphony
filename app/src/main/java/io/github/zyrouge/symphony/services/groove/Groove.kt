package io.github.zyrouge.symphony.services.groove

import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.repositories.AlbumArtistRepository
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import io.github.zyrouge.symphony.services.groove.repositories.ArtistRepository
import io.github.zyrouge.symphony.services.groove.repositories.GenreRepository
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.utils.ActivityUtils
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class Groove(private val symphony: Symphony) : Symphony.Hooks {
    enum class Kind {
        SONG,
        ALBUM,
        ARTIST,
        ALBUM_ARTIST,
        GENRE,
        PLAYLIST,
    }

    val coroutineScope = CoroutineScope(Dispatchers.Default)
    var readyDeferred = CompletableDeferred<Boolean>()

    val exposer = MediaExposer(symphony)
    val song = SongRepository(symphony)
    val album = AlbumRepository(symphony)
    val artist = ArtistRepository(symphony)
    val albumArtist = AlbumArtistRepository(symphony)
    val genre = GenreRepository(symphony)
    val playlist = PlaylistRepository(symphony)

    private suspend fun fetch() {
        coroutineScope.launch {
            awaitAll(
                async { exposer.fetch() },
                async { playlist.fetch() },
            )
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

    private suspend fun clearCache() {
        symphony.database.songCache.clear()
        symphony.database.artworkCache.clear()
        symphony.database.lyricsCache.clear()
    }

    data class FetchOptions(
        val resetInMemoryCache: Boolean = false,
        val resetPersistentCache: Boolean = false,
    )

    fun fetch(options: FetchOptions) {
        coroutineScope.launch {
            if (options.resetInMemoryCache) {
                reset()
            }
            if (options.resetPersistentCache) {
                clearCache()
            }
            fetch()
        }
    }

    override fun onSymphonyReady() {
        coroutineScope.launch {
            fetch()
            readyDeferred.complete(true)
        }
    }

    fun delete(songToDelete: Song) {
        coroutineScope.launch {
            if (delete(songToDelete.uri)) {
                //TODO: if currently playing song gets deleted
                coroutineScope.launch {
                    awaitAll(
                        async { symphony.radio.queue.removeAll(songToDelete.id) },
                        async { song.delete(songToDelete) },
                        async { album.delete(songToDelete) },
                        async { artist.delete(songToDelete) },
                        async { albumArtist.delete(songToDelete) },
                        async { genre.delete(songToDelete) },
                        async { playlist.deleteSong(songToDelete) },
                    )
                }.join()
            }
        }
    }

    internal fun delete(uri: Uri): Boolean {
        val cR = symphony.applicationContext.contentResolver
        //TODO: check if this makes sense, this technically should never get called
        if (cR.persistedUriPermissions.none {
                val parent = DocumentsContract.getTreeDocumentId(it.uri)
                DocumentsContract.getDocumentId(uri).startsWith("${parent}/")
            }) {
            Logger.warn("Deleter", "Don't have Permissions, asking")
            ActivityUtils.makePersistableReadableUri(symphony.applicationContext, uri)
        }
        try {
            DocumentsContract.deleteDocument(cR, uri)
        } catch (e: SecurityException) {
            Logger.error("Deleter", "Couldn't delete: $e")
            Toast.makeText(symphony.applicationContext, "Failed to delete Song", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }
}

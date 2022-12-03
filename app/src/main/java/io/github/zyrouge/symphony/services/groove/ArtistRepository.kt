package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.*
import kotlinx.coroutines.Dispatchers

enum class ArtistSortBy {
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class ArtistRepository(private val symphony: Symphony) {
    private val cached = mutableMapOf<String, Artist>()
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<Artist>(
        options = listOf(
            FuzzySearchOption({ it.artistName })
        )
    )

    fun fetch() {
        symphony.launchInScope(Dispatchers.Default) {
            fetchSync()
        }
    }

    private fun fetchSync() {
        cached.clear()
        val cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Artists.ARTIST + " ASC"
        )
        try {
            val updateDispatcher = GrooveRepositoryUpdateDispatcher {
                onUpdate.dispatch(null)
            }
            cursor?.use {
                while (it.moveToNext()) {
                    kotlin
                        .runCatching { Artist.fromCursor(it) }
                        .getOrNull()
                        ?.let { artist ->
                            cached[artist.artistName] = artist
                            updateDispatcher.increment()
                        }
                }
            }
        } catch (err: Exception) {
            Logger.error("ArtistRepository", "fetch failed: $err")
        }
        onUpdate.dispatch(null)
    }

    fun getArtistArtworkUri(artistName: String) =
        symphony.groove.album.getAlbumOfArtist(artistName)?.let {
            symphony.groove.album.getAlbumArtworkUri(it.albumId)
        } ?: symphony.groove.album.getDefaultAlbumArtworkUri()

    fun createArtistArtworkImageRequest(artistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtistArtworkUri(artistName),
        fallback = Assets.placeholderId,
    )

    fun getAll() = cached.values.toList()
    fun getArtistFromName(artistName: String) = cached[artistName]

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(artists: List<Artist>, by: ArtistSortBy, reversed: Boolean): List<Artist> {
            val sorted = when (by) {
                ArtistSortBy.ARTIST_NAME -> artists.sortedBy { it.artistName }
                ArtistSortBy.TRACKS_COUNT -> artists.sortedBy { it.numberOfTracks }
                ArtistSortBy.ALBUMS_COUNT -> artists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
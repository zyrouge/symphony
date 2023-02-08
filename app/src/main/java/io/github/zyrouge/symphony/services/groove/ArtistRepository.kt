package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.*
import java.util.concurrent.ConcurrentHashMap

enum class ArtistSortBy {
    CUSTOM,
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class ArtistRepository(private val symphony: Symphony) {
    private val cached = ConcurrentHashMap<String, Artist>()
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    internal val searcher = FuzzySearcher<Artist>(
        options = listOf(
            FuzzySearchOption({ it.name })
        )
    )

    fun fetch() {
        if (isUpdating) return
        isUpdating = true
        onUpdate.dispatch(null)
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
                            cached[artist.name] = artist
                            updateDispatcher.increment()
                        }
                }
            }
        } catch (err: Exception) {
            Logger.error("ArtistRepository", "fetch failed: $err")
        }
        isUpdating = false
        onUpdate.dispatch(null)
    }

    fun reset() {
        cached.clear()
        onUpdate.dispatch(null)
    }

    fun getArtistArtworkUri(artistName: String) =
        symphony.groove.album.getAlbumOfArtist(artistName)?.let {
            symphony.groove.album.getAlbumArtworkUri(it.id)
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
                ArtistSortBy.CUSTOM -> artists.toList()
                ArtistSortBy.ARTIST_NAME -> artists.sortedBy { it.name }
                ArtistSortBy.TRACKS_COUNT -> artists.sortedBy { it.numberOfTracks }
                ArtistSortBy.ALBUMS_COUNT -> artists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}

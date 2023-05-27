package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class MediaStoreExposer(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<Long, Song>()

    var explorer = createExplorer()
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private fun emitUpdate(value: Boolean) = _isUpdating.tryEmit(value)

    fun fetch() {
        emitUpdate(true)
        try {
            val cursor = symphony.applicationContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + " != 0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
            )
            cursor?.use {
                val blacklisted = symphony.settings.blacklistFolders.value.toSortedSet()
                val whitelisted = symphony.settings.whitelistFolders.value.toSortedSet()
                val regex = symphony.settings.songsFilterPattern.value
                    ?.let { literal -> Regex(literal, RegexOption.IGNORE_CASE) }

                val additionalMetadataCache = kotlin
                    .runCatching { symphony.database.songCache.read() }
                    .getOrNull()
                val nAdditionalMetadata = mutableMapOf<Long, SongCache.Attributes>()

                while (it.moveToNext()) {
                    kotlin
                        .runCatching {
                            Song.fromCursor(symphony, it) { id ->
                                additionalMetadataCache?.get(id)
                            }
                        }
                        .getOrNull()
                        ?.also { song ->
                            explorer.addRelativePath(GrooveExplorer.Path(song.path))
                            cache[song.id] = song
                        }
                        ?.takeIf { song -> regex?.containsMatchIn(song.path) != false }
                        ?.takeIf { song ->
                            blacklisted
                                .find { x -> song.path.startsWith(x) }
                                ?.let { match ->
                                    whitelisted.any { x ->
                                        x.startsWith(match) && song.path.startsWith(x)
                                    }
                                }
                                ?: true
                        }
                        ?.also { song ->
                            nAdditionalMetadata[song.id] = SongCache.Attributes.fromSong(song)
                            emitSong(song)
                        }
                }
                symphony.database.songCache.update(nAdditionalMetadata)
            }
        } catch (err: Exception) {
            Logger.error("MediaStoreExposer", "fetch failed", err)
        }
        emitUpdate(false)
        emitFinish()
    }

    fun reset() {
        emitUpdate(true)
        explorer = createExplorer()
        cache.clear()
        emitUpdate(false)
    }

    private fun emitSong(song: Song) {
        symphony.groove.albumArtist.onSong(song)
        symphony.groove.album.onSong(song)
        symphony.groove.artist.onSong(song)
        symphony.groove.genre.onSong(song)
        symphony.groove.lyrics.onSong(song)
        symphony.groove.song.onSong(song)
    }

    private fun emitFinish() {
        symphony.groove.albumArtist.onFinish()
        symphony.groove.album.onFinish()
        symphony.groove.artist.onFinish()
        symphony.groove.genre.onFinish()
        symphony.groove.song.onFinish()
    }

    companion object {
        fun createExplorer() = GrooveExplorer.Folder("root")
    }
}

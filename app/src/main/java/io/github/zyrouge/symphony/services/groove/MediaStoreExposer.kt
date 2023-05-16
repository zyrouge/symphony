package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class MediaStoreExposer(private val symphony: Symphony) {
    var explorer = createExplorer()
    val cache = ConcurrentHashMap<Long, Song>()

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
                val blacklisted = symphony.settings.getBlacklistFolders().toSortedSet()
                val whitelisted = symphony.settings.getWhitelistFolders().toSortedSet()
                val regex = symphony.settings.getSongsFilterPattern()
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

    companion object {
        fun createExplorer() = GrooveExplorer.Folder("root")
    }
}

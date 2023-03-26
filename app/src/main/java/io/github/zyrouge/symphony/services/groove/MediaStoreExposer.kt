package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.dispatch
import java.util.concurrent.ConcurrentHashMap

class MediaStoreExposer(private val symphony: Symphony) {
    val onSong = Eventer<Song>()
    val onFetchStart = Eventer.nothing()
    val onFetchEnd = Eventer.nothing()
    var isFetching = false
    var explorer = createExplorer()
    val cache = ConcurrentHashMap<Long, Song>()

    fun fetch() {
        isFetching = true
        val cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )
        try {
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
                            onSong.dispatch(song)
                        }
                }
                symphony.database.songCache.update(nAdditionalMetadata)
            }
        } catch (err: Exception) {
            Logger.error("MediaStoreExposer", "fetch failed: $err")
        }
        isFetching = false
        onFetchEnd.dispatch()
    }

    fun reset() {
        explorer = createExplorer()
        cache.clear()
    }

    companion object {
        fun createExplorer() = GrooveExplorer.Folder("root")
    }
}

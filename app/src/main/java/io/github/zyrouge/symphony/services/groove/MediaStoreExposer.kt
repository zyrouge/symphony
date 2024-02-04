package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.CursorShorty
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.getColumnIndices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

class MediaStoreExposer(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<Long, Song>()

    var explorer = GrooveExplorer.Folder()
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private fun emitUpdate(value: Boolean) {
        _isUpdating.update {
            value
        }
        symphony.groove.onMediaStoreUpdate(value)
    }

    fun fetch() {
        emitUpdate(true)
        try {
            val cursor = symphony.applicationContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projectedColumns.toTypedArray(),
                MediaStore.Audio.Media.IS_MUSIC + " = 1",
                null,
                null
            )
            cursor?.use {
                val shorty = CursorShorty(it, it.getColumnIndices(projectedColumns))

                val blacklisted = symphony.settings.blacklistFolders.value.toSortedSet()
                val whitelisted = symphony.settings.whitelistFolders.value.toSortedSet()
                val regex = symphony.settings.songsFilterPattern.value
                    ?.let { literal -> Regex(literal, RegexOption.IGNORE_CASE) }

                val additionalMetadataCache = kotlin
                    .runCatching { symphony.database.songCache.read() }
                    .getOrNull()
                val nAdditionalMetadata = mutableMapOf<Long, SongCache.Attributes>()

                while (it.moveToNext()) {
                    val path = shorty.getString(AudioColumns.DATA)
                    explorer.addRelativePath(GrooveExplorer.Path(path))
                    val isWhitelisted = true
                        .takeIf { regex?.containsMatchIn(path) ?: true }
                        .takeIf {
                            blacklisted
                                .find { x -> path.startsWith(x) }
                                ?.let { match ->
                                    whitelisted.any { x ->
                                        x.startsWith(match) && path.startsWith(x)
                                    }
                                } ?: true
                        } ?: false
                    if (!isWhitelisted) {
                        continue
                    }

                    kotlin
                        .runCatching {
                            Song.fromCursor(symphony, shorty) { id ->
                                additionalMetadataCache?.get(id)
                            }
                        }
                        .getOrNull()
                        ?.also { song ->
                            cache[song.id] = song
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
        explorer = GrooveExplorer.Folder()
        cache.clear()
        emitUpdate(false)
    }

    private fun emitSong(song: Song) {
        symphony.groove.albumArtist.onSong(song)
        symphony.groove.album.onSong(song)
        symphony.groove.artist.onSong(song)
        symphony.groove.genre.onSong(song)
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
        val projectedColumns = listOf(
            AudioColumns._ID,
            AudioColumns.DATE_MODIFIED,
            AudioColumns.TITLE,
            AudioColumns.TRACK,
            AudioColumns.YEAR,
            AudioColumns.DURATION,
            AudioColumns.ALBUM_ID,
            AudioColumns.ALBUM,
            AudioColumns.ARTIST_ID,
            AudioColumns.ARTIST,
            AudioColumns.COMPOSER,
            AudioColumns.DATE_ADDED,
            AudioColumns.SIZE,
            AudioColumns.DATA
        )

        fun isWhitelisted(
            path: String,
            regex: Regex?,
            blacklisted: List<String>,
            whitelisted: List<String>,
        ): Boolean {
            regex?.let {
                if (!it.containsMatchIn(path)) {
                    return false
                }
            }
            return blacklisted
                .find { x -> path.startsWith(x) }
                ?.let { match ->
                    whitelisted.any { x ->
                        x.startsWith(match) && path.startsWith(x)
                    }
                } ?: true
        }
    }
}

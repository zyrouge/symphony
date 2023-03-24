package io.github.zyrouge.symphony.services.groove

import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.SettingsKeys
import io.github.zyrouge.symphony.services.database.SongCache
import io.github.zyrouge.symphony.utils.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

enum class SongSortBy {
    CUSTOM,
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    DATE_ADDED,
    DATE_MODIFIED,
    COMPOSER,
    ALBUM_ARTIST,
    YEAR,
    FILENAME,
    TRACK_NUMBER,
}

class SongRepository(private val symphony: Symphony) {
    private val cached = ConcurrentHashMap<Long, Song>()
    internal var cachedGenres = ConcurrentHashMap<String, Genre>()
    internal var cachedPaths = ConcurrentHashMap<String, Long>()
    internal var cachedLyrics = ConcurrentHashMap<Long, String>()
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()
    var explorer = createNewExplorer()
    var foldersExplorer = createNewExplorer()

    private val searcher = FuzzySearcher<Song>(
        options = listOf(
            FuzzySearchOption({ it.title }, 3),
            FuzzySearchOption({ it.filename }, 2),
            FuzzySearchOption({ it.artistName }),
            FuzzySearchOption({ it.albumName })
        )
    )

    init {
        symphony.settings.onChange.subscribe { event ->
            if (event == SettingsKeys.songsFilterPattern) {
                fetch()
            }
        }
    }

    fun fetch() {
        if (isUpdating) return
        setGlobalUpdateState(true)
        dispatchGlobalUpdate()
        val cursor = symphony.applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )
        try {
            val updateDispatcher = GrooveRepositoryUpdateDispatcher { dispatchGlobalUpdate() }
            cursor?.use {
                val blacklisted = symphony.settings.getBlacklistFolders().toSortedSet()
                val whitelisted = symphony.settings.getWhitelistFolders().toSortedSet()
                val regex = symphony.settings.getSongsFilterPattern()
                    ?.let { literal -> Regex(literal, RegexOption.IGNORE_CASE) }

                val additionalMetadataCache = kotlin
                    .runCatching { symphony.database.songCache.read() }
                    .getOrNull()
                val lyricsCache = kotlin
                    .runCatching { symphony.database.lyricsCache.read() }
                    .getOrNull()
                val nAdditionalMetadata = mutableMapOf<Long, SongCache.Attributes>()
                val nLyrics = mutableMapOf<String, String>()

                while (it.moveToNext()) {
                    kotlin
                        .runCatching {
                            Song.fromCursor(symphony, it) { id ->
                                additionalMetadataCache?.get(id)
                            }
                        }
                        .getOrNull()
                        ?.let { song ->
                            foldersExplorer.addRelativePath(GrooveExplorer.Path(song.path))
                            song
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
                        ?.let { song ->
                            cached[song.id] = song
                            nAdditionalMetadata[song.id] = SongCache.Attributes.fromSong(song)
                            song.additional.albumArtist?.let { albumArtist ->
                                symphony.groove.albumArtist.cached.compute(albumArtist) { _, value ->
                                    value
                                        ?.apply {
                                            albumIdsSet.add(song.albumId)
                                            numberOfTracks++
                                        } ?: AlbumArtist(
                                        name = albumArtist,
                                        albumIdsSet = mutableSetOf(song.albumId),
                                        numberOfTracks = 1,
                                    )
                                }
                            }
                            song.additional.genre?.let { genre ->
                                cachedGenres.compute(genre) { _, value ->
                                    value
                                        ?.apply { numberOfTracks++ }
                                        ?: Genre(name = genre, numberOfTracks = 1)
                                }
                            }
                            val lyricsCacheKey = "${song.id}-${song.dateModified}"
                            lyricsCache?.get(lyricsCacheKey)?.let { lyrics ->
                                cachedLyrics[song.id] = lyrics
                                nLyrics[lyricsCacheKey] = lyrics
                            }
                            cachedPaths[song.path] = song.id
                            val entity = explorer
                                .addRelativePath(GrooveExplorer.Path(song.path)) as GrooveExplorer.File
                            entity.data = song.id
                            updateDispatcher.increment()
                        }
                }
                symphony.database.songCache.update(nAdditionalMetadata)
                symphony.database.lyricsCache.update(nLyrics)
            }
        } catch (err: Exception) {
            Logger.error("SongRepository", "fetch failed: $err")
        }
        setGlobalUpdateState(false)
        dispatchGlobalUpdate()
    }

    fun reset() {
        cached.clear()
        cachedGenres.clear()
        cachedPaths.clear()
        cachedLyrics.clear()
        explorer = createNewExplorer()
        foldersExplorer = createNewExplorer()
        dispatchGlobalUpdate()
    }

    private fun setGlobalUpdateState(to: Boolean) {
        isUpdating = to
        symphony.groove.albumArtist.isUpdating = to
        symphony.groove.genre.isUpdating = to
    }

    private fun dispatchGlobalUpdate() {
        onUpdate.dispatch(null)
        symphony.groove.albumArtist.onUpdate.dispatch(null)
        symphony.groove.genre.onUpdate.dispatch(null)
    }

    fun getAll() = cached.values.toList()
    private fun getAll(filter: (Song) -> Boolean) = getAll().filter(filter)

    fun getSongWithId(songId: Long) = cached[songId]
    fun hasSongWithId(songId: Long) = getSongWithId(songId) != null

    fun getSongsOfArtist(artistName: String) = getAll { it.artistName == artistName }
    fun getSongsOfAlbum(albumId: Long) = getAll { it.albumId == albumId }
    fun getSongsOfGenre(genre: String) = getAll { it.additional.genre == genre }
    fun getSongsOfAlbumArtist(artistName: String) =
        getAll { it.additional.albumArtist == artistName }

    fun getSongsOfPlaylist(playlistId: String) = symphony.groove.playlist
        .getPlaylistWithId(playlistId)
        ?.songs?.mapNotNull { cached[it] }
        ?: listOf()

    suspend fun getLyrics(song: Song): String? = run {
        try {
            val outputFile = symphony.applicationContext.cacheDir
                .toPath()
                .resolve(song.filename)
                .toFile()
            FileX.ensureFile(outputFile)
            symphony.applicationContext.contentResolver.openInputStream(song.uri)
                ?.use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            val lyrics = AudioTaggerX.getLyrics(outputFile)
            outputFile.delete()
            lyrics?.let {
                cachedLyrics[song.id] = lyrics
                symphony.database.lyricsCache.read().let { lyricsCache ->
                    val mLyricsCache = lyricsCache.entries.toMutableList().let { entries ->
                        entries
                            .subList(max(0, entries.size - 50), entries.size)
                            .associate { it.toPair() }
                            .toMutableMap()
                    }
                    mLyricsCache["${song.id}-${song.dateModified}"] = lyrics
                    symphony.database.lyricsCache.update(mLyricsCache.toMap())
                }
            }
            return lyrics
        } catch (err: Exception) {
            Logger.error("SongRepository", "fetch lyrics failed: $err")
        }
        return null
    }

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(songs: List<Song>, by: SongSortBy, reversed: Boolean): List<Song> {
            val sorted = when (by) {
                SongSortBy.CUSTOM -> songs.toList()
                SongSortBy.TITLE -> songs.sortedBy { it.title }
                SongSortBy.ARTIST -> songs.sortedBy { it.artistName }
                SongSortBy.ALBUM -> songs.sortedBy { it.albumName }
                SongSortBy.DURATION -> songs.sortedBy { it.duration }
                SongSortBy.DATE_ADDED -> songs.sortedBy { it.dateAdded }
                SongSortBy.DATE_MODIFIED -> songs.sortedBy { it.dateModified }
                SongSortBy.COMPOSER -> songs.sortedBy { it.composer }
                SongSortBy.ALBUM_ARTIST -> songs.sortedBy { it.additional.albumArtist }
                SongSortBy.YEAR -> songs.sortedBy { it.year }
                SongSortBy.FILENAME -> songs.sortedBy { it.filename }
                SongSortBy.TRACK_NUMBER -> songs.sortedBy { it.trackNumber }
            }
            return if (reversed) sorted.reversed() else sorted
        }

        fun createNewExplorer() = GrooveExplorer.Folder("root")
    }
}

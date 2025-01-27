package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.ArtworkIndex
import io.github.zyrouge.symphony.services.groove.entities.Composer
import io.github.zyrouge.symphony.services.groove.entities.ComposerSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Genre
import io.github.zyrouge.symphony.services.groove.entities.GenreSongMapping
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeFolder
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeLyricFile
import io.github.zyrouge.symphony.services.groove.entities.MediaTreeSongFile
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongLyric
import io.github.zyrouge.symphony.utils.ActivityUtils
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.DocumentFileX
import io.github.zyrouge.symphony.utils.ImagePreserver
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.utils.SimplePath
import io.github.zyrouge.symphony.utils.concurrentSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MediaExposer(private val symphony: Symphony) {
    private val _isUpdating = MutableStateFlow<Boolean>(false)
    val isUpdating get() = _isUpdating.asStateFlow()

    suspend fun fetch() {
        _isUpdating.update { true }
        coroutineScope {
            awaitAll(
                async { scanMediaTree() },
                async { scanPlaylists() },
            )
        }
        _isUpdating.update { false }
    }

    suspend fun scanMediaTree() {
        try {
            val context = symphony.applicationContext
            val folderUris = symphony.settings.mediaFolders.value
            val scanner = MediaTreeScanner.create(symphony)
            folderUris.map { x ->
                ActivityUtils.makePersistableReadableUri(context, x)
                DocumentFileX.fromTreeUri(context, x)?.let {
                    val path = SimplePath(DocumentFileX.getParentPathOfTreeUri(x) ?: it.name)
                    with(Dispatchers.IO) {
                        scanner.scanMediaFolder(path, scanner.rootFolder, it)
                    }
                }
            }
            scanner.cleanup()
        } catch (err: Exception) {
            Logger.error("MediaExposer", "fetch failed", err)
        }
    }

    suspend fun scanPlaylists() {
        try {
            val playlists = symphony.database.playlists.valuesLocalOnly()
            val playlistsToBeUpdated = mutableListOf<Playlist>()
            val playlistIdsToBeDeletedInMapping = mutableListOf<String>()
            val playlistSongMappingToBeInserted = mutableListOf<PlaylistSongMapping>()
            for (exPlaylist in playlists) {
                val playlistId = exPlaylist.id
                val uri = exPlaylist.uri!!
                playlistIdsToBeDeletedInMapping.add(playlistId)
                val extended = Playlist.parse(symphony, playlistId, uri)
                playlistsToBeUpdated.add(extended.playlist)
                var nextPlaylistSongMapping: PlaylistSongMapping? = null
                for (i in (extended.songPaths.size - 1) downTo 0) {
                    val x = extended.songPaths[i]
                    val playlistSongMapping = PlaylistSongMapping(
                        id = symphony.database.playlistSongMappingIdGenerator.next(),
                        playlistId = playlistId,
                        songId = null,
                        songPath = x,
                        isStart = i == 0,
                        nextId = nextPlaylistSongMapping?.id,
                    )
                    playlistSongMappingToBeInserted.add(playlistSongMapping)
                    nextPlaylistSongMapping = playlistSongMapping
                }
            }
            symphony.database.playlists.update(*playlistsToBeUpdated.toTypedArray())
            symphony.database.playlistSongMapping.deletePlaylistIds(playlistIdsToBeDeletedInMapping)
            symphony.database.playlistSongMapping.insert(*playlistSongMappingToBeInserted.toTypedArray())
        } catch (err: Exception) {
            Logger.error("MediaExposer", "playlist fetch failed", err)
        }
    }

    private data class MediaTreeScanner(
        val symphony: Symphony,
        val rootFolder: MediaTreeFolder,
        val folderStaleIds: ConcurrentSet<String>,
        val songFileStaleIds: ConcurrentSet<String>,
        val lyricFileStaleIds: ConcurrentSet<String>,
        val songStaleIds: ConcurrentSet<String>,
        val artworkIndexCache: ConcurrentHashMap<String, ArtworkIndex>,
        val artworkStaleFiles: ConcurrentSet<String>,
        val filter: MediaFilter,
        val songParseOptions: MediaTreeSongFile.ParseOptions,
    ) {
        suspend fun scanMediaFolder(
            path: SimplePath,
            parent: MediaTreeFolder,
            document: DocumentFileX,
        ) {
            if (!filter.isWhitelisted(path.pathString)) {
                return
            }
            val exFolder = symphony.database.mediaTreeFolders.findByName(parent.id, document.name)
            scanMediaFolder(path, parent, exFolder, document)
        }

        suspend fun scanMediaFolder(
            path: SimplePath,
            parent: MediaTreeFolder,
            exFolder: MediaTreeFolder?,
            document: DocumentFileX,
        ) {
            if (!filter.isWhitelisted(path.pathString)) {
                return
            }
            if (exFolder?.dateModified == document.lastModified) {
                folderStaleIds.remove(exFolder.id)
                return
            }
            val folder = exFolder ?: MediaTreeFolder(
                id = symphony.database.mediaTreeFoldersIdGenerator.next(),
                parentId = parent.id,
                name = document.name,
                isHead = false,
                uri = document.uri,
                dateModified = 0, // updated after scan
            )
            folderStaleIds.remove(folder.id)
            if (exFolder == null) {
                symphony.database.mediaTreeFolders.insert(folder)
            }
            scanMediaFolderChildren(path, folder, document)
            symphony.database.mediaTreeFolders.update(
                folder.copy(dateModified = document.lastModified),
            )
        }

        private suspend fun scanMediaFolderChildren(
            path: SimplePath,
            folder: MediaTreeFolder,
            document: DocumentFileX,
        ) {
            coroutineScope {
                val folders = symphony.database.mediaTreeFolders.entriesNameMapped(folder.id)
                val songFiles = symphony.database.mediaTreeSongFiles.entriesNameMapped(folder.id)
                val lyricFiles = symphony.database.mediaTreeLyricFiles.entriesNameMapped(folder.id)
                document.list().mapNotNull {
                    val nPath = path.join(it.name)
                    if (it.isDirectory) {
                        return@mapNotNull async {
                            scanMediaFolder(nPath, folder, folders[it.name], it)
                        }
                    }
                    if (nPath.extension == "lrc") {
                        return@mapNotNull async {
                            scanLyricFile(nPath, folder, lyricFiles[it.name], document)
                        }
                    }
                    if (document.mimeType.startsWith("audio/")) {
                        return@mapNotNull async {
                            scanSongFile(nPath, folder, songFiles[it.name], document)
                        }
                    }
                    null
                }.awaitAll()
            }
        }

        suspend fun scanSongFile(
            path: SimplePath,
            parent: MediaTreeFolder,
            exFile: MediaTreeSongFile?,
            document: DocumentFileX,
        ) {
            val exArtworkIndex = artworkIndexCache[exFile?.id]
            val skipArtworkParsing = exArtworkIndex != null && exArtworkIndex.let {
                exArtworkIndex.file == null || artworkStaleFiles.contains(exArtworkIndex.file)
            }
            if (skipArtworkParsing && exFile?.dateModified == document.lastModified) {
                songFileStaleIds.remove(exFile.id)
                exArtworkIndex.file?.let { artworkStaleFiles.remove(it) }
                return
            }
            val id = exFile?.id ?: symphony.database.mediaTreeSongFilesIdGenerator.next()
            val extended = MediaTreeSongFile.parse(path, parent, document, id, songParseOptions)
            val file = extended.file
            songFileStaleIds.remove(file.id)
            val artworkFile = extended.artwork?.let {
                val extension = when (it.mimeType) {
                    "image/jpg", "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    "_" -> "_"
                    else -> null
                }
                if (extension == null) {
                    return@let null
                }
                val artworkId = symphony.database.artworksIdGenerator.next()
                val quality = symphony.settings.artworkQuality.value
                if (quality.maxSide == null && extension != "_") {
                    val name = "$artworkId.$extension"
                    symphony.database.artworks.get(name).writeBytes(it.data)
                    return@let name
                }
                val bitmap = BitmapFactory.decodeByteArray(it.data, 0, it.data.size)
                val name = "$artworkId.jpg"
                FileOutputStream(symphony.database.artworks.get(name)).use { writer ->
                    ImagePreserver
                        .resize(bitmap, quality)
                        .compress(Bitmap.CompressFormat.JPEG, 100, writer)
                }
                name
            }
            val artworkIndex = ArtworkIndex(songId = id, file = artworkFile)
            artworkIndex.file?.let {
                artworkStaleFiles.remove(it)
            }
            symphony.database.mediaTreeSongFiles.update(file)
            if (exArtworkIndex?.file != artworkIndex.file) {
                symphony.database.artworkIndices.upsert(artworkIndex)
            }
            extended.lyrics?.let {
                symphony.database.songLyrics.upsert(SongLyric(id, it))
            }
            when {
                exFile == null -> symphony.database.mediaTreeSongFiles.insert(file)
                else -> symphony.database.mediaTreeSongFiles.update(file)
            }
            if (file.duration.milliseconds < symphony.settings.minSongDuration.value.seconds) {
                return
            }
            val exSong = symphony.database.songs.findByPath(file.path)
            val song = when {
                exSong != null -> exSong.copy(
                    title = file.title,
                    trackNumber = file.trackNumber,
                    trackTotal = file.trackTotal,
                    discNumber = file.discNumber,
                    discTotal = file.discTotal,
                    date = file.date,
                    year = file.year,
                    duration = file.duration,
                    bitrate = file.bitrate,
                    samplingRate = file.samplingRate,
                    channels = file.channels,
                    encoder = file.encoder,
                    dateModified = file.dateModified,
                    size = file.size,
                    filename = file.name,
                    uri = file.uri,
                    path = file.path,
                )

                else -> Song(
                    id = id,
                    title = file.title,
                    trackNumber = file.trackNumber,
                    trackTotal = file.trackTotal,
                    discNumber = file.discNumber,
                    discTotal = file.discTotal,
                    date = file.date,
                    year = file.year,
                    duration = file.duration,
                    bitrate = file.bitrate,
                    samplingRate = file.samplingRate,
                    channels = file.channels,
                    encoder = file.encoder,
                    dateModified = file.dateModified,
                    size = file.size,
                    filename = file.name,
                    uri = file.uri,
                    path = file.path,
                )
            }
            when {
                exSong == null -> symphony.database.songs.insert(song)
                else -> symphony.database.songs.update(song)
            }
            val exAlbum = file.album?.let { albumName ->
                symphony.database.albums.findByName(albumName)
            }
            val album = file.album?.let { albumName ->
                val songYear = file.year ?: file.date?.year
                when {
                    exAlbum != null -> {
                        val startYear = when {
                            songYear == null -> exAlbum.startYear
                            exAlbum.startYear == null -> songYear
                            else -> min(songYear, exAlbum.startYear)
                        }
                        val endYear = when {
                            songYear == null -> exAlbum.endYear
                            exAlbum.endYear == null -> songYear
                            else -> max(songYear, exAlbum.endYear)
                        }
                        exAlbum.copy(startYear = startYear, endYear = endYear)
                    }

                    else -> Album(
                        id = symphony.database.albumsIdGenerator.next(),
                        name = albumName,
                        startYear = songYear,
                        endYear = songYear,
                    )
                }
            }
            if (album != null) {
                when {
                    exAlbum == null -> symphony.database.albums.insert(album)
                    else -> symphony.database.albums.update(album)
                }
                val albumSongMapping = AlbumSongMapping(albumId = album.id, songId = id)
                symphony.database.albumSongMapping.upsert(albumSongMapping)
            }
            val artists = file.artists + file.albumArtists
            val artistsToBeInserted = mutableListOf<Artist>()
            val artistSongMappingsToBeUpserted = mutableListOf<ArtistSongMapping>()
            val albumArtistMappingsToBeUpserted = mutableMapOf<String, AlbumArtistMapping>()
            val exArtistIds = symphony.database.artists.entriesByNameNameIdMapped(artists)
            val artistIds = mutableMapOf<String, String>()
            artistIds.putAll(exArtistIds)
            for (artistName in artists) {
                val exArtistId = exArtistIds[artistName]
                val artistId = exArtistId ?: symphony.database.artistsIdGenerator.next()
                if (exArtistId == null) {
                    val artist = Artist(id = artistId, name = artistName)
                    artistIds[artistName] = artist.id
                    artistsToBeInserted.add(artist)
                }
                val artistSongMapping = ArtistSongMapping(artistId = artistId, songId = id)
                artistSongMappingsToBeUpserted.add(artistSongMapping)
                if (album != null) {
                    val albumArtistMapping = AlbumArtistMapping(
                        albumId = album.id,
                        artistId = artistId,
                        isAlbumArtist = file.albumArtists.contains(artistName),
                    )
                    albumArtistMappingsToBeUpserted[artistName] = albumArtistMapping
                }
            }
            symphony.database.artists.insert(*artistsToBeInserted.toTypedArray())
            symphony.database.artistSongMapping.upsert(*artistSongMappingsToBeUpserted.toTypedArray())
            symphony.database.albumArtistMapping.upsert(*albumArtistMappingsToBeUpserted.values.toTypedArray())
            val exComposerIds =
                symphony.database.composers.entriesByNameNameIdMapped(file.composers)
            val composersToBeInserted = mutableListOf<Composer>()
            val composerSongMappingsToBeUpserted = mutableListOf<ComposerSongMapping>()
            for (composerName in file.composers) {
                val exComposerId = exComposerIds[composerName]
                val composerId = exComposerId ?: symphony.database.composersIdGenerator.next()
                if (exComposerId == null) {
                    val composer = Composer(id = composerId, name = composerName)
                    composersToBeInserted.add(composer)
                }
                val composerSongMapping = ComposerSongMapping(composerId = composerId, songId = id)
                composerSongMappingsToBeUpserted.add(composerSongMapping)
            }
            symphony.database.composers.insert(*composersToBeInserted.toTypedArray())
            symphony.database.composerSongMapping.upsert(*composerSongMappingsToBeUpserted.toTypedArray())
            val exGenreIds = symphony.database.genres.entriesByNameNameIdMapped(file.genres)
            val genresToBeInserted = mutableListOf<Genre>()
            val genreSongMappingsToBeUpserted = mutableListOf<GenreSongMapping>()
            for (genreName in file.genres) {
                val exGenreId = exGenreIds[genreName]
                val genreId = exGenreId ?: symphony.database.genresIdGenerator.next()
                if (exGenreId == null) {
                    val genre = Genre(id = genreId, name = genreName)
                    genresToBeInserted.add(genre)
                }
                val genreSongMapping = GenreSongMapping(genreId = genreId, songId = id)
                genreSongMappingsToBeUpserted.add(genreSongMapping)
            }
            symphony.database.genres.insert(*genresToBeInserted.toTypedArray())
            symphony.database.genreSongMapping.upsert(*genreSongMappingsToBeUpserted.toTypedArray())
        }

        suspend fun scanLyricFile(
            @Suppress("Unused") path: SimplePath,
            parent: MediaTreeFolder,
            exFile: MediaTreeLyricFile?,
            document: DocumentFileX,
        ) {
            if (exFile?.dateModified == document.lastModified) {
                lyricFileStaleIds.remove(exFile.id)
                return
            }
            val file = MediaTreeLyricFile(
                id = exFile?.id ?: symphony.database.mediaTreeLyricFilesIdGenerator.next(),
                parentId = parent.id,
                name = document.name,
                dateModified = document.lastModified,
                uri = document.uri,
            )
            lyricFileStaleIds.remove(file.id)
            when {
                exFile == null -> symphony.database.mediaTreeLyricFiles.insert(file)
                else -> symphony.database.mediaTreeLyricFiles.update(file)
            }
        }

        suspend fun cleanup() {
            try {
                symphony.database.mediaTreeSongFiles.delete(songFileStaleIds)
            } catch (err: Exception) {
                Logger.warn("MediaExposer", "trimming song files failed", err)
            }
            for (x in artworkStaleFiles) {
                try {
                    symphony.database.artworks.get(x).delete()
                } catch (err: Exception) {
                    Logger.warn("MediaExposer", "deleting artwork failed", err)
                }
            }
        }

        companion object {
            suspend fun create(symphony: Symphony): MediaTreeScanner {
                val filter = MediaFilter(
                    symphony.settings.songsFilterPattern.value,
                    symphony.settings.blacklistFolders.value.toSortedSet(),
                    symphony.settings.whitelistFolders.value.toSortedSet()
                )
                val rootFolder = getTreeRootFolder(symphony)
                val folderIds = symphony.database.mediaTreeFolders.ids(rootFolder.id)
                val songFileIds = symphony.database.mediaTreeSongFiles.ids(rootFolder.id)
                val lyricFileIds = symphony.database.mediaTreeLyricFiles.ids(rootFolder.id)
                val songIds = symphony.database.songs.ids()
                return MediaTreeScanner(
                    symphony = symphony,
                    rootFolder = rootFolder,
                    folderStaleIds = concurrentSetOf(folderIds),
                    songFileStaleIds = concurrentSetOf(songFileIds),
                    lyricFileStaleIds = concurrentSetOf(lyricFileIds),
                    songStaleIds = concurrentSetOf(songIds),
                    artworkIndexCache = ConcurrentHashMap(symphony.database.artworkIndices.entriesSongIdMapped()),
                    artworkStaleFiles = concurrentSetOf(symphony.database.artworks.all()),
                    filter = filter,
                    songParseOptions = MediaTreeSongFile.ParseOptions.create(symphony),
                )
            }

            private suspend fun getTreeRootFolder(symphony: Symphony): MediaTreeFolder {
                symphony.database.mediaTreeFolders.findHeadByName(MEDIA_TREE_ROOT_NAME)?.let {
                    return it
                }
                val root = MediaTreeFolder(
                    id = symphony.database.mediaTreeFoldersIdGenerator.next(),
                    parentId = null,
                    name = MEDIA_TREE_ROOT_NAME,
                    isHead = true,
                    dateModified = 0,
                    uri = null,
                )
                symphony.database.mediaTreeFolders.insert(root)
                return root
            }
        }
    }

    private class MediaFilter(
        pattern: String?,
        private val blacklisted: Set<String>,
        private val whitelisted: Set<String>,
    ) {
        private val regex = pattern?.let { Regex(it, RegexOption.IGNORE_CASE) }

        fun isWhitelisted(path: String): Boolean {
            regex?.let {
                if (!it.containsMatchIn(path)) {
                    return false
                }
            }
            val bFilter = blacklisted.findLast {
                path.startsWith(it)
            }
            if (bFilter == null) {
                return true
            }
            val wFilter = whitelisted.findLast {
                it.startsWith(bFilter) && path.startsWith(it)
            }
            return wFilter != null
        }
    }

    companion object {
        const val MEDIA_TREE_ROOT_NAME = "root"
    }
}

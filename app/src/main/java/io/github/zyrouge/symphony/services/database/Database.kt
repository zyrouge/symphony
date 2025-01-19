package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.ArtworkStore
import io.github.zyrouge.symphony.utils.KeyGenerator

class Database(symphony: Symphony) {
    private val persistent = PersistentDatabase.create(symphony)

    val albumArtistSongsIdGenerator = KeyGenerator.TimeIncremental()
    val albumArtistsIdGenerator = KeyGenerator.TimeIncremental()
    val albumSongsIdGenerator = KeyGenerator.TimeIncremental()
    val albumsIdGenerator = KeyGenerator.TimeIncremental()
    val artistSongsIdGenerator = KeyGenerator.TimeIncremental()
    val artistsIdGenerator = KeyGenerator.TimeIncremental()
    val mediaTreeFoldersIdGenerator = KeyGenerator.TimeIncremental()
    val mediaTreeSongFilesIdGenerator = KeyGenerator.TimeIncremental()
    val mediaTreeLyricsFilesIdGenerator = KeyGenerator.TimeIncremental()
    val genreSongsIdGenerator = KeyGenerator.TimeIncremental()
    val genreIdGenerator = KeyGenerator.TimeIncremental()
    val playlistSongsIdGenerator = KeyGenerator.TimeIncremental()
    val playlistsIdGenerator = KeyGenerator.TimeIncremental()
    val songFilesIdGenerator = KeyGenerator.TimeIncremental()
    val songLyricsIdGenerator = KeyGenerator.TimeIncremental()
    val songsIdGenerator = KeyGenerator.TimeIncremental()

    val albumArtistSongs get() = persistent.albumArtistSongs()
    val albumArtists get() = persistent.albumArtists()
    val albumSongs get() = persistent.albumSongs()
    val albums get() = persistent.albums()
    val artistSongs get() = persistent.artistSongs()
    val artists get() = persistent.artists()
    val artwork = ArtworkStore(symphony)
    val artworkIndices get() = persistent.artworkIndices()
    val genreSongs get() = persistent.genreSongs()
    val genre get() = persistent.genre()
    val mediaTreeFolders get() = persistent.mediaTreeFolders()
    val mediaTreeSongFiles get() = persistent.mediaTreeSongFiles()
    val mediaTreeLyricsFiles get() = persistent.mediaTreeLyricsFiles()
    val playlistSongs get() = persistent.playlistSongs()
    val playlists get() = persistent.playlists()
    val songFiles get() = persistent.songFiles()
    val songLyrics get() = persistent.songLyrics()
    val songs get() = persistent.songs()
}

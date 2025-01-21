package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.ArtworkStore
import io.github.zyrouge.symphony.utils.KeyGenerator

class Database(symphony: Symphony) {
    val persistent = PersistentDatabase.create(symphony)

    val albumsIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val albumArtistMappingIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val albumSongMappingIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val artistsIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val artistSongMappingIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val artworksIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val artworkIndicesIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val genresIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val genreSongMappingIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val mediaTreeFoldersIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val mediaTreeSongFilesIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val mediaTreeLyricFilesIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val playlistsIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val playlistSongMappingIdGenerator = KeyGenerator.TimeCounterRandomMix()
    val songLyricsIdGenerator = KeyGenerator.TimeCounterRandomMix()

    val albums get() = persistent.albums()
    val albumArtistMapping get() = persistent.albumArtistMapping()
    val albumSongMapping get() = persistent.albumSongMapping()
    val artists get() = persistent.artists()
    val artistSongMapping get() = persistent.artistSongMapping()
    val artworks = ArtworkStore(symphony)
    val artworkIndices get() = persistent.artworkIndices()
    val genres get() = persistent.genre()
    val genreSongMapping get() = persistent.genreSongMapping()
    val mediaTreeFolders get() = persistent.mediaTreeFolders()
    val mediaTreeLyricFiles get() = persistent.mediaTreeLyricFiles()
    val mediaTreeSongFiles get() = persistent.mediaTreeSongFiles()
    val playlists get() = persistent.playlists()
    val playlistSongMapping get() = persistent.playlistSongMapping()
    val songLyrics get() = persistent.songLyrics()
    val songs get() = persistent.songs()
}

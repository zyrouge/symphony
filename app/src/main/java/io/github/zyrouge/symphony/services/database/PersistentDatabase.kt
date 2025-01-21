package io.github.zyrouge.symphony.services.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.AlbumArtistMappingStore
import io.github.zyrouge.symphony.services.database.store.AlbumSongMappingStore
import io.github.zyrouge.symphony.services.database.store.AlbumStore
import io.github.zyrouge.symphony.services.database.store.ArtistSongMappingStore
import io.github.zyrouge.symphony.services.database.store.ArtistStore
import io.github.zyrouge.symphony.services.database.store.ArtworkIndexStore
import io.github.zyrouge.symphony.services.database.store.GenreSongMappingStore
import io.github.zyrouge.symphony.services.database.store.GenreStore
import io.github.zyrouge.symphony.services.database.store.MediaTreeFolderStore
import io.github.zyrouge.symphony.services.database.store.MediaTreeLyricFileStore
import io.github.zyrouge.symphony.services.database.store.MediaTreeSongFileStore
import io.github.zyrouge.symphony.services.database.store.PlaylistSongMappingStore
import io.github.zyrouge.symphony.services.database.store.PlaylistStore
import io.github.zyrouge.symphony.services.database.store.SongLyricStore
import io.github.zyrouge.symphony.services.database.store.SongStore
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping
import io.github.zyrouge.symphony.services.groove.entities.AlbumSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.ArtistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.ArtworkIndex
import io.github.zyrouge.symphony.services.groove.entities.Genre
import io.github.zyrouge.symphony.services.groove.entities.GenreSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.PlaylistSongMapping
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongLyric
import io.github.zyrouge.symphony.utils.RoomConvertors

@Database(
    version = 1,
    entities = [
        AlbumArtistMapping::class,
        AlbumSongMapping::class,
        Album::class,
        ArtistSongMapping::class,
        Artist::class,
        ArtworkIndex::class,
        GenreSongMapping::class,
        Genre::class,
        MediaTreeFolderStore::class,
        MediaTreeLyricFileStore::class,
        MediaTreeSongFileStore::class,
        PlaylistSongMapping::class,
        Playlist::class,
        SongLyric::class,
        Song::class,
    ],
)
@TypeConverters(RoomConvertors::class)
abstract class PersistentDatabase : RoomDatabase() {
    abstract fun albumArtistMapping(): AlbumArtistMappingStore
    abstract fun albumSongMapping(): AlbumSongMappingStore
    abstract fun albums(): AlbumStore
    abstract fun artistSongMapping(): ArtistSongMappingStore
    abstract fun artists(): ArtistStore
    abstract fun artworkIndices(): ArtworkIndexStore
    abstract fun genreSongMapping(): GenreSongMappingStore
    abstract fun genre(): GenreStore
    abstract fun mediaTreeFolders(): MediaTreeFolderStore
    abstract fun mediaTreeLyricFiles(): MediaTreeLyricFileStore
    abstract fun mediaTreeSongFiles(): MediaTreeSongFileStore
    abstract fun playlistSongMapping(): PlaylistSongMappingStore
    abstract fun playlists(): PlaylistStore
    abstract fun songLyrics(): SongLyricStore
    abstract fun songs(): SongStore

    companion object {
        const val DATABASE_NAME = "symphony_persistent"

        fun create(symphony: Symphony) = Room
            .databaseBuilder(
                symphony.applicationContext,
                PersistentDatabase::class.java,
                DATABASE_NAME
            )
            .build()
    }
}

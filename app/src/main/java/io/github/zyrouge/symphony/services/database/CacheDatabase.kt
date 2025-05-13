package io.github.zyrouge.symphony.services.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.SongCacheStore
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.RoomConvertors

@Database(
    entities = [Song::class],
    version = 3,
    autoMigrations = [AutoMigration(1, 2, CacheDatabase.Migration1To2::class), AutoMigration(2, 3)]
)
@TypeConverters(RoomConvertors::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun songs(): SongCacheStore

    companion object {
        fun create(symphony: Symphony) = Room
            .databaseBuilder(
                symphony.applicationContext,
                CacheDatabase::class.java,
                "cache"
            )
            .build()
    }

    @DeleteColumn("songs", "minBitrate")
    @DeleteColumn("songs", "maxBitrate")
    @DeleteColumn("songs", "bitsPerSample")
    @DeleteColumn("songs", "samples")
    @DeleteColumn("songs", "codec")
    class Migration1To2 : AutoMigrationSpec
}

package io.github.zyrouge.symphony.services.database.store

import androidx.room.Dao
import androidx.room.Insert
import io.github.zyrouge.symphony.services.groove.entities.AlbumArtistMapping

@Dao
interface AlbumArtistMappingStore {
    @Insert
    suspend fun insert(vararg entities: AlbumArtistMapping)
}

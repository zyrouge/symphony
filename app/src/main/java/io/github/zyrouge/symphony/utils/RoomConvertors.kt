package io.github.zyrouge.symphony.utils

import android.net.Uri
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("unused")
class RoomConvertors {
    @TypeConverter
    fun serializeUri(value: String) = Uri.parse(value)

    @TypeConverter
    fun deserializeUri(value: Uri) = value.toString()

    @TypeConverter
    fun serializeStringSet(value: String) = Json.decodeFromString<Set<String>>(value)

    @TypeConverter
    fun deserializeStringSet(value: Set<String>) = Json.encodeToString(value)

    @TypeConverter
    fun serializeStringList(value: String) = Json.decodeFromString<List<String>>(value)

    @TypeConverter
    fun deserializeStringList(value: List<String>) = Json.encodeToString(value)
}

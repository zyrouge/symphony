package io.github.zyrouge.symphony.services.i18n

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class Translation(container: _Container) : _Translation(container) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        @OptIn(ExperimentalSerializationApi::class)
        fun fromInputStream(input: InputStream): Translation {
            val container = json.decodeFromStream<_Container>(input)
            return Translation(container)
        }
    }
}

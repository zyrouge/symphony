package io.github.zyrouge.symphony.services.i18n

import io.github.zyrouge.symphony.utils.RelaxedJsonDecoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class Translation(container: _Container) : _Translation(container) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromInputStream(input: InputStream): Translation {
            val container = RelaxedJsonDecoder.decodeFromStream<_Container>(input)
            return Translation(container)
        }
    }
}

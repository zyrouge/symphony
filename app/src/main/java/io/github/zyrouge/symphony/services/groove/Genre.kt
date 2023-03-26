package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable

@Immutable
data class Genre(
    val name: String,
    var numberOfTracks: Int,
)

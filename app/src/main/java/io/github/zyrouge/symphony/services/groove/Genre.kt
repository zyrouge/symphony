package io.github.zyrouge.symphony.services.groove

data class Genre(
    val name: String,
    // NOTE: mutable cause we handle this
    var numberOfTracks: Int,
)

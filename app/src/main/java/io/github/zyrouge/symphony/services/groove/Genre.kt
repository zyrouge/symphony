package io.github.zyrouge.symphony.services.groove

data class Genre(
    val genre: String,
    // NOTE: mutable cause we handle this
    var numberOfTracks: Int,
)

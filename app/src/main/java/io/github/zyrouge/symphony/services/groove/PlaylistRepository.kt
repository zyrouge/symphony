package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony

enum class PlaylistSortBy {
    TITLE,
    TRACKS_COUNT,
}

class PlaylistRepository(private val symphony: Symphony) {}

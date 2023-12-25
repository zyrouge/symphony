package io.github.zyrouge.symphony.ui.helpers

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import io.github.zyrouge.symphony.services.groove.GrooveKinds

object RoutesParameters {
    const val ArtistRouteArtistName = "artistName"
    const val AlbumRouteAlbumId = "albumId"
    const val AlbumArtistRouteArtistName = "albumArtistName"
    const val GenreRouteGenre = "genre"
    const val PlaylistRoutePlaylistId = "playlistId"
    const val SearchRouteInitialChip = "initialChip"
}

object RoutesBuilder {
    fun buildArtistRoute(artistName: String) = "artist/${encodeParam(artistName)}"
    fun buildAlbumRoute(albumId: Long) = buildAlbumRoute(albumId.toString())
    fun buildAlbumRoute(albumId: String) = "album/$albumId"
    fun buildAlbumArtistRoute(artistName: String) = "album_artist/${encodeParam(artistName)}"
    fun buildGenreRoute(genre: String) = "genre/${encodeParam(genre)}"
    fun buildPlaylistRoute(playlistId: String) = "playlist/${encodeParam(playlistId)}"
    fun buildSearchRoute(kind: GrooveKinds? = null) = RoutesBuilder.buildSearchRoute(kind?.name)
    fun buildSearchRoute(initialChip: String? = null) =
        "search/${encodeParam(initialChip ?: "null")}"

    private val encodeParamChars = object {
        val percent = "%" to "%25"
        val slash = "/" to "%2F"
    }

    fun encodeParam(value: String) = value
        .replace(encodeParamChars.percent.first, encodeParamChars.percent.second)
        .replace(encodeParamChars.slash.first, encodeParamChars.slash.second)

    fun decodeParam(value: String) = value
        .replace(encodeParamChars.percent.second, encodeParamChars.percent.first)
        .replace(encodeParamChars.slash.second, encodeParamChars.slash.first)
}

sealed class Routes(val route: String) {
    constructor(
        fn: (b: RoutesBuilder, p: RoutesParameters) -> String,
    ) : this(fn(RoutesBuilder, RoutesParameters))

    data object Home : Routes("home")
    data object NowPlaying : Routes("now_playing")
    data object Queue : Routes("queue")
    data object Settings : Routes("settings")
    data object Search : Routes({ b, p -> b.buildSearchRoute("{${p.SearchRouteInitialChip}}") })
    data object Artist : Routes({ b, p -> b.buildArtistRoute("{${p.ArtistRouteArtistName}}") })
    data object Album : Routes({ b, p -> b.buildAlbumRoute("{${p.AlbumRouteAlbumId}}") })
    data object AlbumArtist :
        Routes({ b, p -> b.buildAlbumArtistRoute("{${p.AlbumArtistRouteArtistName}}") })

    data object Genre : Routes({ b, p -> b.buildGenreRoute("{${p.GenreRouteGenre}}") })
    data object Playlist :
        Routes({ b, p -> b.buildPlaylistRoute("{${p.PlaylistRoutePlaylistId}}") })

    data object Lyrics : Routes("lyrics")
}

fun NavHostController.navigate(route: Routes) = navigate(route.route)

fun NavBackStackEntry.getRouteArgument(key: String) =
    arguments?.getString(key)?.let { RoutesBuilder.decodeParam(it) }

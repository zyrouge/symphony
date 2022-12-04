package io.github.zyrouge.symphony.ui.helpers

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

object RoutesParameters {
    const val ArtistRouteArtistName = "artistName"
    const val AlbumRouteAlbumId = "albumId"
    const val AlbumArtistRouteArtistName = "albumArtistName"
    const val GenreRouteGenre = "genre"
}

object RoutesBuilder {
    fun buildArtistRoute(artistName: String) = "artist/${encodeParam(artistName)}"
    fun buildAlbumRoute(albumId: Long) = buildAlbumRoute(albumId.toString())
    fun buildAlbumRoute(albumId: String) = "album/$albumId"
    fun buildAlbumArtistRoute(artistName: String) = "album_artist/${encodeParam(artistName)}"
    fun buildGenreRoute(genre: String) = "genre/${encodeParam(genre)}"

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
        fn: (b: RoutesBuilder, p: RoutesParameters) -> String
    ) : this(fn(RoutesBuilder, RoutesParameters))

    object Home : Routes("home")
    object NowPlaying : Routes("now_playing")
    object Queue : Routes("queue")
    object Settings : Routes("settings")
    object Search : Routes("search")

    object Artist : Routes({ b, p -> b.buildArtistRoute("{${p.ArtistRouteArtistName}}") })
    object Album : Routes({ b, p -> b.buildAlbumRoute("{${p.AlbumRouteAlbumId}}") })
    object AlbumArtist :
        Routes({ b, p -> b.buildAlbumArtistRoute("{${p.AlbumArtistRouteArtistName}}") })

    object Genre : Routes({ b, p -> b.buildGenreRoute("{${p.GenreRouteGenre}}") })
}

fun NavHostController.navigate(route: Routes) = navigate(route.route)

fun NavBackStackEntry.getRouteArgument(key: String) =
    arguments?.getString(key)?.let { RoutesBuilder.decodeParam(it) }

package io.github.zyrouge.symphony.ui.helpers

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

object RoutesParameters {
    const val ArtistRouteArtistName = "artistName"
    const val AlbumRouteAlbumId = "albumId"
}

object RoutesBuilder {
    fun buildArtistRoute(artistName: String) = "artist/${encodeParam(artistName)}"
    fun buildAlbumRoute(albumId: Long) = buildAlbumRoute(albumId.toString())
    fun buildAlbumRoute(albumId: String) = "album/$albumId"

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
    object Home : Routes("home")
    object NowPlaying : Routes("now_playing")
    object Queue : Routes("queue")
    object Settings : Routes("settings")
    object Search : Routes("search")

    object Artist :
        Routes(RoutesBuilder.buildArtistRoute("{${RoutesParameters.ArtistRouteArtistName}}"))

    object Album :
        Routes(RoutesBuilder.buildAlbumRoute("{${RoutesParameters.AlbumRouteAlbumId}}"))
}

fun NavHostController.navigate(route: Routes) = navigate(route.route)

fun NavBackStackEntry.getRouteArgument(key: String) =
    arguments?.getString(key)?.let { RoutesBuilder.decodeParam(it) }

package io.github.zyrouge.symphony.ui.helpers

import androidx.navigation.NavHostController

object RoutesParameters {
    const val ArtistRouteArtistName = "artistName"
    const val AlbumRouteAlbumId = "albumId"
}

object RoutesBuilder {
    fun buildArtistRoute(artistName: String) = "artist/$artistName"
    fun buildAlbumRoute(albumId: Long) = buildAlbumRoute(albumId.toString())
    fun buildAlbumRoute(albumId: String) = "album/$albumId"
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

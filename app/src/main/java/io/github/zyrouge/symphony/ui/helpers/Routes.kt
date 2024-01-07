package io.github.zyrouge.symphony.ui.helpers

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import io.github.zyrouge.symphony.services.groove.GrooveKinds

abstract class Route(val route: String) {
    class Simple(route: String) : Route(route)

    abstract class Parameterized<T>(route: String) : Route(route) {
        override fun template() = "$route/{$ARGUMENT_NAME}"
        abstract fun build(param: T): String

        companion object {
            const val ARGUMENT_NAME = "param"
        }
    }

    class StringParameterized(route: String) : Parameterized<String>(route) {
        override fun build(param: String) = withParam(param)
    }

    open fun template() = route

    protected fun withParam(param: String) = "$route/${encodeParam(param)}"
    protected fun encodeParam(param: String): String = Uri.encode(param)
}

data object Routes {
    val Home = Route.Simple("home")
    val NowPlaying = Route.Simple("now_playing")
    val Queue = Route.Simple("queue")
    val Settings = Route.Simple("settings")
    val Search = object : Route.Parameterized<GrooveKinds?>("search") {
        override fun build(param: GrooveKinds?) = withParam(param?.name ?: "null")
    }
    val Artist = Route.StringParameterized("artist")
    val Album = Route.StringParameterized("album")
    val AlbumArtist = Route.StringParameterized("album_artist")
    val Genre = Route.StringParameterized("genre")
    val Playlist = Route.StringParameterized("playlist")
    val Lyrics = Route.Simple("lyrics")
}

fun NavHostController.navigate(route: Route.Simple) = navigate(route.route)

fun NavBackStackEntry.getRouteArgument(key: String) = arguments?.getString(key)
fun NavBackStackEntry.getRouteParameter() =
    getRouteArgument(Route.Parameterized.ARGUMENT_NAME) ?: ""

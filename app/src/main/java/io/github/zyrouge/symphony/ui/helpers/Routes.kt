package io.github.zyrouge.symphony.ui.helpers

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.view.SettingsViewElements

abstract class Route(val route: String) {
    abstract class Simple(route: String) : Route(route) {
        override fun template() = route
    }

    abstract class Parameterized<T>(route: String) : Route(route) {
        override fun template() = "$route/{$PARAM_ARGUMENT_NAME}"
        abstract fun buildParam(param: T): String
        fun build(param: T) = "$route/${encodeArgument(buildParam(param))}"
    }

    abstract class StringParameterized(route: String) : Parameterized<String>(route) {
        override fun template() = "$route/{$PARAM_ARGUMENT_NAME}"
        override fun buildParam(param: String) = param
    }

    abstract fun template(): String
    open fun arguments(): List<NamedNavArgument> = emptyList()

    companion object {
        const val PARAM_ARGUMENT_NAME = "param"

        fun encodeArgument(param: String): String = Uri.encode(param)
    }
}

object Routes {
    object Home : Route.Simple("home")

    object NowPlaying : Route.Simple("now_playing")
    object Queue : Route.Simple("queue")

    object Settings : Route("settings") {
        const val ELEMENT_ARGUMENT_NAME = "element"

        override fun template() = "$route?$ELEMENT_ARGUMENT_NAME={$ELEMENT_ARGUMENT_NAME}"
        override fun arguments() = listOf(
            navArgument(ELEMENT_ARGUMENT_NAME) {
                type = NavType.StringType
                nullable = true
            },
        )

        fun build(settingsViewElement: SettingsViewElements? = null) = buildString {
            append(route)
            if (settingsViewElement != null) {
                append("?$ELEMENT_ARGUMENT_NAME=${encodeArgument(settingsViewElement.name)}")
            }
        }
    }

    object Search : Route.Parameterized<Groove.Kinds?>("search") {
        override fun buildParam(param: Groove.Kinds?) = param?.name ?: "null"
    }

    object Artist : Route.StringParameterized("artist")
    object Album : Route.StringParameterized("album")
    object AlbumArtist : Route.StringParameterized("album_artist")
    object Genre : Route.StringParameterized("genre")
    object Playlist : Route.StringParameterized("playlist")
    object Lyrics : Route.Simple("lyrics")
}

fun NavHostController.navigateTo(route: Route.Simple) = navigate(route.route)
fun NavHostController.navigateTo(route: String) = navigate(route)

fun NavBackStackEntry.getRouteArgument(key: String) = arguments?.getString(key)
fun NavBackStackEntry.getRouteParameter() = getRouteArgument(Route.PARAM_ARGUMENT_NAME) ?: ""

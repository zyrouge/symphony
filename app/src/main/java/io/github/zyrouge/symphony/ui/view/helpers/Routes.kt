package io.github.zyrouge.symphony.ui.view.helpers

sealed class Routes(val route: String) {
    object Home : Routes("home")
    object NowPlaying : Routes("now_playing")
    object Queue : Routes("queue")
    object Settings : Routes("settings")
}

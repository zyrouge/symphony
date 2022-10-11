package io.github.zyrouge.symphony

import android.content.Context
import io.github.zyrouge.symphony.services.PermissionsManager
import io.github.zyrouge.symphony.services.Player
import io.github.zyrouge.symphony.services.groove.GrooveManager
import io.github.zyrouge.symphony.services.i18n.Translations

object Symphony {
    lateinit var activity: MainActivity
    lateinit var permissions: PermissionsManager
    lateinit var t: Translations
    lateinit var groove: GrooveManager
    lateinit var player: Player

    fun init(activity: MainActivity) {
        this.activity = activity
        permissions = PermissionsManager()
        permissions.init()
        t = Translations.default
        groove = GrooveManager()
        groove.init()
        player = Player()
        player.init()
    }

    val context: Context
        get() = activity.applicationContext
}
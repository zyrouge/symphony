package io.github.zyrouge.symphony

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import io.github.zyrouge.symphony.services.PermissionsManager
import io.github.zyrouge.symphony.services.Player
import io.github.zyrouge.symphony.services.SettingsManager
import io.github.zyrouge.symphony.services.groove.GrooveManager
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.services.i18n.Translator

class Symphony(application: Application) : AndroidViewModel(application) {
    val permission = PermissionsManager(this)
    val settings = SettingsManager(this)
    val groove = GrooveManager(this)
    val player = Player(this)

    val translator = Translator(this)
    val t: Translations
        get() = translator.t

    val applicationContext: Context
        get() = getApplication<Application>().applicationContext
}
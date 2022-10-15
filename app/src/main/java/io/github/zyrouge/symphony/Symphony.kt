package io.github.zyrouge.symphony

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import io.github.zyrouge.symphony.services.Player
import io.github.zyrouge.symphony.services.groove.GrooveManager
import io.github.zyrouge.symphony.services.i18n.Translations

class Symphony(application: Application) : AndroidViewModel(application) {
    val t = Translations.default
    val groove = GrooveManager(this)
    val player = Player(this)

    val applicationContext: Context
        get() = getApplication<Application>().applicationContext
}
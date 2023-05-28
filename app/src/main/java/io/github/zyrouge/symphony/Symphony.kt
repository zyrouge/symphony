package io.github.zyrouge.symphony

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.PermissionsManager
import io.github.zyrouge.symphony.services.SettingsManager
import io.github.zyrouge.symphony.services.database.Database
import io.github.zyrouge.symphony.services.groove.GrooveManager
import io.github.zyrouge.symphony.services.i18n.Translator
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.utils.AndroidXShorty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface SymphonyHooks {
    fun onSymphonyReady() {}
    fun onSymphonyPause() {}
    fun onSymphonyDestroy() {}
}

class Symphony(application: Application) : AndroidViewModel(application), SymphonyHooks {
    val shorty = AndroidXShorty(this)
    val permission = PermissionsManager(this)
    val settings = SettingsManager(this)
    val database = Database(this)
    val groove = GrooveManager(this)
    val radio = Radio(this)
    val translator = Translator(this)

    var t by mutableStateOf(translator.getCurrentTranslation())

    val applicationContext: Context
        get() = getApplication<Application>().applicationContext
    var closeApp: (() -> Unit)? = null
    private var isReady = false
    private var hooks = listOf(this, radio, groove)

    fun ready() {
        if (isReady) return
        isReady = true
        notifyHooks { onSymphonyReady() }
    }

    fun pause() {
        notifyHooks { onSymphonyPause() }
    }

    fun destroy() {
        notifyHooks { onSymphonyDestroy() }
    }

    override fun onSymphonyReady() {
        checkVersion()
        viewModelScope.launch {
            translator.onChange { nTranslation ->
                t = nTranslation
            }
        }
    }

    private fun notifyHooks(fn: SymphonyHooks.() -> Unit) {
        hooks.forEach { fn.invoke(it) }
    }

    private fun checkVersion() {
        if (!settings.checkForUpdates.value) return
        viewModelScope.launch {
            val latestVersion = withContext(Dispatchers.IO) {
                AppMeta.fetchLatestVersion()
            } ?: return@launch
            withContext(Dispatchers.Main) {
                if (settings.showUpdateToast.value && AppMeta.version != latestVersion) {
                    Toast.makeText(
                        applicationContext,
                        t.NewVersionAvailableX(latestVersion),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }
}

package io.github.zyrouge.symphony

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.PermissionsManager
import io.github.zyrouge.symphony.services.SettingsManager
import io.github.zyrouge.symphony.services.groove.GrooveManager
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.services.i18n.Translator
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.utils.Eventer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface SymphonyHooks {
    fun onSymphonyReady() {}
    fun onSymphonyPause() {}
    fun onSymphonyDestroy() {}
}

class Symphony(application: Application) : AndroidViewModel(application), SymphonyHooks {
    val permission = PermissionsManager(this)
    val settings = SettingsManager(this)
    val groove = GrooveManager(this)
    val radio = Radio(this)

    val translator = Translator(this)
    val t: Translations
        get() = translator.t

    val applicationContext: Context
        get() = getApplication<Application>().applicationContext
    private var isReady = false
    private var hooks = listOf<SymphonyHooks>(this, radio)

    fun ready() {
        if (isReady) return
        isReady = true
        hooks.forEach { it.onSymphonyReady() }
    }

    fun pause() {
        hooks.forEach { it.onSymphonyPause() }
    }

    fun destroy() {
        hooks.forEach { it.onSymphonyDestroy() }
    }

    override fun onSymphonyReady() {
        super.onSymphonyReady()
        createService()
        checkVersion()
    }

    private fun createService() {
        SymphonyService.bridge.subscribe {
            when (it) {
                SymphonyServiceBridgeEvents.ON_TASK_REMOVED -> destroy()
            }
        }
        Intent(applicationContext, SymphonyService::class.java).also { intent ->
            applicationContext.startService(intent)
        }
    }

    private fun checkVersion() {
        viewModelScope.launch {
            val latestVersion = withContext(Dispatchers.IO) { AppMeta.fetchLatestVersion() }
            latestVersion?.let {
                withContext(Dispatchers.Main) {
                    if (settings.getCheckForUpdates() && AppMeta.version != it) {
                        Toast
                            .makeText(
                                applicationContext,
                                t.newVersionAvailableX(it),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            }
        }
    }
}

enum class SymphonyServiceBridgeEvents {
    ON_TASK_REMOVED
}

class SymphonyService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        bridge.dispatch(SymphonyServiceBridgeEvents.ON_TASK_REMOVED)
    }

    companion object {
        val bridge = Eventer<SymphonyServiceBridgeEvents>()
    }
}

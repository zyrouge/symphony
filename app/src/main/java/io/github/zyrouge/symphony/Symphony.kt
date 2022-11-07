package io.github.zyrouge.symphony

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import io.github.zyrouge.symphony.services.PermissionsManager
import io.github.zyrouge.symphony.services.SettingsManager
import io.github.zyrouge.symphony.services.groove.GrooveManager
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.services.i18n.Translator
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.utils.Eventer

interface SymphonyHooks {
    fun onSymphonyReady() {}
    fun onSymphonyPause() {}
    fun onSymphonyDestroy() {}
}

class Symphony(application: Application) : AndroidViewModel(application) {
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
    private var hooks = listOf<SymphonyHooks>(radio)

    fun ready() {
        if (isReady) return
        isReady = true
        createService()
        hooks.forEach { it.onSymphonyReady() }
    }

    fun pause() {
        hooks.forEach { it.onSymphonyPause() }
    }

    fun destroy() {
        hooks.forEach { it.onSymphonyDestroy() }
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

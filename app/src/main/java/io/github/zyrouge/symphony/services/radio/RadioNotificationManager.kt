package io.github.zyrouge.symphony.services.radio

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer

class RadioNotificationManager(val symphony: Symphony) {
    private var manager = NotificationManagerCompat.from(symphony.applicationContext)
    private var lastNotification: Notification? = null

    enum class State {
        PREPARING,
        READY,
        DESTROYED,
    }

    private var state = State.DESTROYED
    private val service: RadioNotificationService?
        get() = RadioNotificationService.instance
    private val hasService: Boolean
        get() = state == State.READY && service != null

    fun prepare() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                RadioNotification.CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            ).run {
                setName(symphony.applicationContext.getString(R.string.app_name))
                setLightsEnabled(false)
                setVibrationEnabled(false)
                setShowBadge(false)
                build()
            }
        )
        RadioNotificationService.events.subscribe {
            when (it) {
                RadioNotificationServiceEvents.START -> onServiceStart()
                RadioNotificationServiceEvents.STOP -> onServiceStop()
            }
        }
    }

    fun cancel() {
        destroy()
        RadioNotificationService.destroy()
    }

    fun notify(notification: Notification) {
        if (!hasService) {
            createService()
            lastNotification = notification
            return
        }
        manager.notify(RadioNotification.NOTIFICATION_ID, notification)
    }

    private fun destroy() {
        if (state == State.DESTROYED) return
        state = State.DESTROYED
        lastNotification = null
        manager.cancel(RadioNotification.CHANNEL_ID, RadioNotification.NOTIFICATION_ID)
    }

    private fun createService() {
        if (hasService || state == State.PREPARING) return
        val intent = Intent(symphony.applicationContext, RadioNotificationService::class.java)
        symphony.applicationContext.startForegroundService(intent)
        state = State.PREPARING
    }

    private fun onServiceStart() {
        state = State.READY
        lastNotification?.let { notification ->
            lastNotification = null
            service!!.startForeground(RadioNotification.NOTIFICATION_ID, notification)
        }
    }

    private fun onServiceStop() {
        destroy()
    }
}

enum class RadioNotificationServiceEvents {
    START,
    STOP,
}

class RadioNotificationService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        events.dispatch(RadioNotificationServiceEvents.START)
        return START_NOT_STICKY
    }

    companion object {
        val events = Eventer<RadioNotificationServiceEvents>()
        var instance: RadioNotificationService? = null

        fun destroy() {
            instance?.let {
                instance = null
                it.stopForeground(STOP_FOREGROUND_REMOVE)
                it.stopSelf()
                events.dispatch(RadioNotificationServiceEvents.STOP)
            }
        }
    }
}

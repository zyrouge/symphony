package io.github.zyrouge.symphony.services.radio

import android.Manifest
import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony

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
                NotificationManagerCompat.IMPORTANCE_LOW,
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
                RadioNotificationService.Events.START -> onServiceStart()
                RadioNotificationService.Events.STOP -> onServiceStop()
            }
        }
    }

    fun cancel() {
        destroyNotification()
        RadioNotificationService.destroy()
    }

    fun notify(notification: Notification) {
        if (!hasService) {
            createService()
            lastNotification = notification
            return
        }
        if (
            ActivityCompat.checkSelfPermission(
                symphony.applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(RadioNotification.NOTIFICATION_ID, notification)
        }
    }

    private fun destroyNotification() {
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
            ServiceCompat.startForeground(
                service!!,
                RadioNotification.NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    0
                }
            )
        }
    }

    private fun onServiceStop() {
        destroyNotification()
    }
}

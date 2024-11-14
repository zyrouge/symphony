package io.github.zyrouge.symphony.services.radio

import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Intent
import android.os.IBinder
import io.github.zyrouge.symphony.utils.Eventer

class RadioNotificationService : Service() {
    enum class Event {
        START,
        STOP,
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        events.dispatch(Event.START)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy(false)
    }

    companion object {
        val events = Eventer<Event>()
        var instance: RadioNotificationService? = null

        fun destroy(stop: Boolean = true) {
            instance?.let {
                instance = null
                if (stop) {
                    it.stopForeground(STOP_FOREGROUND_REMOVE)
                    it.stopSelf()
                }
                events.dispatch(Event.STOP)
            }
        }
    }
}

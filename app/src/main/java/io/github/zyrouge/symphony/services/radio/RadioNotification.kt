package io.github.zyrouge.symphony.services.radio

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony


class RadioNotification(private val symphony: Symphony) {
    private var manager = RadioNotificationManager(symphony)
    private var usable = false

    fun start() {
        manager.prepare()
        usable = true
    }

    fun cancel() {
        usable = false
        manager.cancel()
    }

    fun update(req: RadioSessionUpdateRequest) {
        if (!usable) return
        NotificationCompat.Builder(
            symphony.applicationContext,
            CHANNEL_ID
        ).run {
            setSmallIcon(R.drawable.material_icon_music_note)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(
                PendingIntent.getActivity(
                    symphony.applicationContext,
                    0,
                    Intent(symphony.applicationContext, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
            setContentTitle(req.song.title)
            setContentText(req.song.artistName)
            setLargeIcon(req.artworkBitmap)
            setOngoing(req.isPlaying)
            addAction(
                createAction(
                    R.drawable.material_icon_skip_previous,
                    symphony.t.previous,
                    RadioSession.ACTION_PREVIOUS
                )
            )
            addAction(
                when {
                    req.isPlaying -> createAction(
                        R.drawable.material_icon_pause,
                        symphony.t.play,
                        RadioSession.ACTION_PLAY_PAUSE
                    )
                    else -> createAction(
                        R.drawable.material_icon_play,
                        symphony.t.pause,
                        RadioSession.ACTION_PLAY_PAUSE
                    )
                }
            )
            addAction(
                createAction(
                    R.drawable.material_icon_skip_next,
                    symphony.t.next,
                    RadioSession.ACTION_NEXT
                )
            )
            addAction(
                createAction(
                    R.drawable.material_icon_stop,
                    symphony.t.stop,
                    RadioSession.ACTION_STOP
                )
            )
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(symphony.radio.session.mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            kotlin.runCatching {
                manager.notify(build())
            }
        }
    }

    private fun createAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            icon, title, createActionIntent(action)
        ).build()
    }

    private fun createActionIntent(action: String): PendingIntent {
        return PendingIntent.getBroadcast(
            symphony.applicationContext,
            0,
            Intent(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val CHANNEL_ID = "${R.string.app_name}_media_notification"
        const val NOTIFICATION_ID = 69421
    }
}

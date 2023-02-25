package io.github.zyrouge.symphony.services.radio

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RadioNotification(private val symphony: Symphony) {
    private var manager = RadioNotificationManager(symphony)
    private var usable = false
    private var currentSongId: Long? = null
    private var currentUpdateJob: Job? = null

    fun start() {
        manager.prepare()
        usable = true
    }

    fun cancel() {
        usable = false
        manager.cancel()
    }

    fun update() {
        currentUpdateJob?.cancel()
        currentUpdateJob = symphony.groove.coroutineScope.launch { updateAsync() }
    }

    private suspend fun updateAsync() {
        if (!usable) return
        currentSongId = symphony.radio.queue.currentPlayingSong?.id
        symphony.radio.queue.currentPlayingSong?.let { song ->
            val cover = getSongArtworkBitmap(song)
            val isPlaying = symphony.radio.isPlaying

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
                setContentTitle(song.title)
                setContentText(song.artistName)
                setLargeIcon(cover)
                setOngoing(isPlaying)
                addAction(
                    createAction(
                        R.drawable.material_icon_skip_previous,
                        symphony.t.previous,
                        RadioSession.ACTION_PREVIOUS
                    )
                )
                addAction(
                    when {
                        isPlaying -> createAction(
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
                // NOTE: final check before notifying and excessive prevention
                //       since android sucks at concurrency
                if (currentSongId == song.id) {
                    kotlin.runCatching {
                        manager.notify(build())
                    }
                }
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

    private var defaultArtworkBitmap: Bitmap? = null
    private var currentArtworkBitmap: Pair<Long, Bitmap?>? = null

    private fun getDefaultArtworkBitmap(): Bitmap {
        return defaultArtworkBitmap ?: run {
            val bitmap = BitmapFactory.decodeResource(
                symphony.applicationContext.resources,
                Assets.placeholderId,
            )
            defaultArtworkBitmap = bitmap
            bitmap
        }
    }

    private suspend fun getSongArtworkBitmap(song: Song): Bitmap {
        if (currentArtworkBitmap?.first != song.id) {
            val result = symphony.applicationContext.imageLoader
                .execute(song.createArtworkImageRequest(symphony).build())
            val size = symphony.applicationContext.resources.displayMetrics.widthPixels
            // NOTE: final check before caching
            if (currentSongId == song.id) {
                currentArtworkBitmap = song.id to result.drawable?.toBitmap(size, size)
            }
        }
        return currentArtworkBitmap?.second ?: getDefaultArtworkBitmap()
    }

    companion object {
        const val CHANNEL_ID = "${R.string.app_name}_media_notification"
        const val NOTIFICATION_ID = 69421
    }
}

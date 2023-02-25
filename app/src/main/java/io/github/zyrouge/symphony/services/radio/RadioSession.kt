package io.github.zyrouge.symphony.services.radio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony

class RadioSession(val symphony: Symphony) {
    val mediaSession = MediaSessionCompat(
        symphony.applicationContext,
        MEDIA_SESSION_ID
    )
    val notification = RadioNotification(symphony)

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                Log.i("SymLog", action)
                handleAction(action)
            }
        }
    }

    fun start() {
        symphony.applicationContext.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(ACTION_PLAY_PAUSE)
                addAction(ACTION_PREVIOUS)
                addAction(ACTION_NEXT)
                addAction(ACTION_STOP)
            }
        )
        mediaSession.setCallback(
            object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    super.onPlay()
                    handleAction(ACTION_PLAY_PAUSE)
                }

                override fun onPause() {
                    super.onPause()
                    handleAction(ACTION_PLAY_PAUSE)
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    handleAction(ACTION_PREVIOUS)
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()
                    handleAction(ACTION_NEXT)
                }

                override fun onStop() {
                    super.onStop()
                    handleAction(ACTION_STOP)
                }

                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    symphony.radio.seek(pos.toInt())
                }

                override fun onRewind() {
                    super.onRewind()
                    val duration = symphony.settings.getSeekBackDuration()
                    symphony.radio.shorty.seekFromCurrent(-duration)
                }

                override fun onFastForward() {
                    super.onFastForward()
                    val duration = symphony.settings.getSeekForwardDuration()
                    symphony.radio.shorty.seekFromCurrent(duration)
                }

                override fun onMediaButtonEvent(intent: Intent?): Boolean {
                    val handled = super.onMediaButtonEvent(intent)
                    if (handled) return true
                    val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent?.getParcelableExtra(
                            Intent.EXTRA_KEY_EVENT,
                            KeyEvent::class.java,
                        )
                    } else {
                        intent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }
                    return when (keyEvent?.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                        KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            handleAction(ACTION_PREVIOUS)
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            handleAction(ACTION_NEXT)
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_CLOSE,
                        KeyEvent.KEYCODE_MEDIA_STOP -> {
                            handleAction(ACTION_STOP)
                            true
                        }
                        else -> false
                    }
                }
            }
        )
        notification.start()
        symphony.radio.onUpdate.subscribe {
            when (it) {
                RadioEvents.StartPlaying,
                RadioEvents.PausePlaying,
                RadioEvents.ResumePlaying,
                RadioEvents.SongStaged,
                RadioEvents.SongSeeked -> update()
                RadioEvents.QueueEnded -> cancel()
                else -> {}
            }
        }
    }

    fun handleAction(action: String) {
        when (action) {
            ACTION_PLAY_PAUSE -> symphony.radio.shorty.playPause()
            ACTION_PREVIOUS -> symphony.radio.shorty.previous()
            ACTION_NEXT -> symphony.radio.shorty.skip()
            ACTION_STOP -> symphony.radio.stop()
        }
    }

    fun cancel() {
        notification.cancel()
        mediaSession.isActive = false
    }

    fun destroy() {
        cancel()
        symphony.applicationContext.unregisterReceiver(receiver)
    }

    fun update() {
        val song = symphony.radio.queue.currentPlayingSong ?: return
        val playbackPosition = symphony.radio.currentPlaybackPosition ?: PlaybackPosition.zero
        val isPlaying = symphony.radio.isPlaying

        ensureEnabled()
        mediaSession.run {
            setMetadata(
                MediaMetadataCompat.Builder().run {
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistName)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
                    putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        symphony.groove.album
                            .getAlbumArtworkUri(song.albumId)
                            .toString()
                    )
                    putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        playbackPosition.total.toLong()
                    )
                    build()
                }
            )
            setPlaybackState(
                PlaybackStateCompat.Builder().run {
                    setState(
                        if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                        else PlaybackStateCompat.STATE_PAUSED,
                        playbackPosition.played.toLong(),
                        1f
                    )
                    setActions(
                        PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_PAUSE
                                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                or PlaybackStateCompat.ACTION_STOP
                                or PlaybackStateCompat.ACTION_REWIND
                                or PlaybackStateCompat.ACTION_FAST_FORWARD
                                or PlaybackStateCompat.ACTION_SEEK_TO
                    )
                    build()
                }
            )
        }
        notification.update()
    }

    private fun ensureEnabled() {
        if (!mediaSession.isActive) {
            mediaSession.isActive = true
        }
    }

    companion object {
        const val MEDIA_SESSION_ID = "${R.string.app_name}_media_session"

        const val ACTION_PLAY_PAUSE = "${R.string.app_name}_play_pause"
        const val ACTION_PREVIOUS = "${R.string.app_name}_previous"
        const val ACTION_NEXT = "${R.string.app_name}_next"
        const val ACTION_STOP = "${R.string.app_name}_stop"
    }
}

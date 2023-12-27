package io.github.zyrouge.symphony.services.radio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContract
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import kotlinx.coroutines.launch


data class RadioSessionUpdateRequest(
    val song: Song,
    val artworkUri: Uri,
    val artworkUriString: String,
    val artworkBitmap: Bitmap,
    val playbackPosition: PlaybackPosition,
    val isPlaying: Boolean,
)

class RadioSession(val symphony: Symphony) {
    val artworkCacher = RadioArtworkCacher(symphony)
    val mediaSession = MediaSessionCompat(
        symphony.applicationContext,
        MEDIA_SESSION_ID
    )
    val notification = RadioNotification(symphony)

    private var currentSongId: Long? = null
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                handleAction(action)
            }
        }
    }

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // As per Tiramisu requirements, we need to add Context attributes.
            symphony.applicationContext.registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(ACTION_PLAY_PAUSE)
                    addAction(ACTION_PREVIOUS)
                    addAction(ACTION_NEXT)
                    addAction(ACTION_STOP)
                },
                Context.RECEIVER_EXPORTED
                // https://developer.android.com/reference/android/content/Context#RECEIVER_EXPORTED
                // really, RECEIVER_EXPORTED and RECEIVER_NOT_EXPORTED makes no difference.
                // the notification appears perfectly, Pano Scrobbler sees it,
                // Wear OS can send signals to play/pause the app, other media apps can pause it,
                // no clue what the difference here is... but here we are.
            )
        } else {
            symphony.applicationContext.registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(ACTION_PLAY_PAUSE)
                    addAction(ACTION_PREVIOUS)
                    addAction(ACTION_NEXT)
                    addAction(ACTION_STOP)
                }
            )
        }
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
                    symphony.radio.seek(pos)
                }

                override fun onRewind() {
                    super.onRewind()
                    val duration = symphony.settings.seekBackDuration.value
                    symphony.radio.shorty.seekFromCurrent(-duration)
                }

                override fun onFastForward() {
                    super.onFastForward()
                    val duration = symphony.settings.seekForwardDuration.value
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
                        @Suppress("DEPRECATION")
                        intent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }
                    return when (keyEvent?.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                        KeyEvent.KEYCODE_MEDIA_REWIND,
                        -> {
                            handleAction(ACTION_PREVIOUS)
                            true
                        }

                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            handleAction(ACTION_NEXT)
                            true
                        }

                        KeyEvent.KEYCODE_MEDIA_CLOSE,
                        KeyEvent.KEYCODE_MEDIA_STOP,
                        -> {
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
                RadioEvents.SongSeeked,
                -> update()

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

    fun createEqualizerActivityContract() = object : ActivityResultContract<Unit, Unit>() {
        override fun createIntent(
            context: Context,
            input: Unit,
        ) = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, symphony.applicationContext.packageName)
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, symphony.radio.audioSessionId ?: 0)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ) {
        }
    }

    private fun update() {
        symphony.groove.coroutineScope.launch {
            updateAsync()
        }
    }

    private suspend fun updateAsync() {
        val song = symphony.radio.queue.currentSongId
            ?.let { symphony.groove.song.get(it) } ?: return
        currentSongId = song.id

        val artworkUri = symphony.groove.song.getArtworkUri(song.id)
        val artworkUriString = artworkUri.toString()
        val artworkBitmap = artworkCacher.getArtwork(song)
        val playbackPosition = symphony.radio.currentPlaybackPosition
            ?: PlaybackPosition(played = 0L, total = song.duration)
        val isPlaying = symphony.radio.isPlaying

        val req = RadioSessionUpdateRequest(
            song = song,
            artworkUri = artworkUri,
            artworkUriString = artworkUriString,
            artworkBitmap = artworkBitmap,
            playbackPosition = playbackPosition,
            isPlaying = isPlaying,
        )
        if (currentSongId != song.id) return

        updateSession(req)
        notification.update(req)
    }

    private fun updateSession(req: RadioSessionUpdateRequest) {
        ensureEnabled()
        mediaSession.run {
            setMetadata(
                MediaMetadataCompat.Builder().run {
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, req.song.title)
                    if (req.song.artists.isNotEmpty()) {
                        putString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                            req.song.artists.joinToString()
                        )
                    }
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, req.song.album)
                    req.artworkUriString.let {
                        putString(MediaMetadataCompat.METADATA_KEY_ART_URI, it)
                        putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, it)
                        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, it)
                    }
                    req.artworkBitmap.let {
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ART, it)
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)
                        putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, it)
                    }
                    putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        req.playbackPosition.total.toLong()
                    )
                    build()
                }
            )
            setPlaybackState(
                PlaybackStateCompat.Builder().run {
                    setState(
                        when {
                            req.isPlaying -> PlaybackStateCompat.STATE_PLAYING
                            else -> PlaybackStateCompat.STATE_PAUSED
                        },
                        req.playbackPosition.played.toLong(),
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
    }

    private fun ensureEnabled() {
        if (!mediaSession.isActive) {
            mediaSession.isActive = true
        }
    }

    companion object {
        val MEDIA_SESSION_ID = "${R.string.app_name}_media_session"

        val ACTION_PLAY_PAUSE = "${R.string.app_name}_play_pause"
        val ACTION_PREVIOUS = "${R.string.app_name}_previous"
        val ACTION_NEXT = "${R.string.app_name}_next"
        val ACTION_STOP = "${R.string.app_name}_stop"
    }
}

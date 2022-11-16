package io.github.zyrouge.symphony.services.radio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.github.zyrouge.symphony.Symphony

class RadioNativeReceiver(private val symphony: Symphony) : BroadcastReceiver() {
    fun start() {
        symphony.applicationContext.registerReceiver(
            this,
            IntentFilter().apply {
                addAction(Intent.ACTION_HEADSET_PLUG)
            }
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let { action ->
            when (action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    intent.extras?.getInt("state")?.let {
                        when (it) {
                            0 -> onHeadphonesDisconnect()
                            1 -> onHeadphonesConnect()
                            else -> {}
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun onHeadphonesConnect() {
        if (!symphony.radio.hasPlayer) return
        if (!symphony.radio.isPlaying && symphony.settings.getPlayOnHeadphonesConnect()) {
            symphony.radio.resume()
        }
    }

    private fun onHeadphonesDisconnect() {
        if (!symphony.radio.hasPlayer) return
        if (symphony.radio.isPlaying && symphony.settings.getPauseOnHeadphonesDisconnect()) {
            symphony.radio.pauseInstant()
        }
    }
}

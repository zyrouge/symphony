package io.github.zyrouge.symphony.services.radio

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class RadioNativeLibraryService : MediaLibraryService() {
    class Callback : MediaLibrarySession.Callback

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
//        val symphony = Symphony.globalInstance ?: return null
//        return MediaLibrarySession.Builder(this, symphony.radio.media, Callback()).build()
        return null
    }
}

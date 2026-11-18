package io.github.zyrouge.symphony.services.groove

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.ImagePreserver
import java.io.FileOutputStream

class AndroidArtworkSink(private val symphony: Symphony) : ArtworkSink {
    override fun store(plan: CoverPlan, picture: EmbeddedPicture?): String? {
        val name = plan.fileName
        if (picture == null || name == null || plan.mode == CoverMode.NONE) {
            return null
        }
        when (plan.mode) {
            CoverMode.RAW_COPY -> {
                symphony.database.artworkCache.get(name).writeBytes(picture.data)
            }
            CoverMode.JPEG_RESIZE -> {
                val bitmap = BitmapFactory.decodeByteArray(picture.data, 0, picture.data.size)
                val quality = symphony.settings.artworkQuality.value
                FileOutputStream(symphony.database.artworkCache.get(name)).use { writer ->
                    ImagePreserver.resize(bitmap, quality)
                        .compress(Bitmap.CompressFormat.JPEG, 100, writer)
                }
            }
            CoverMode.NONE -> return null
        }
        return name
    }
}

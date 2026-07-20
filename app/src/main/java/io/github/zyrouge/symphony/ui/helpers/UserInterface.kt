package io.github.zyrouge.symphony.ui.helpers

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.ui.unit.Dp
import coil.request.ImageRequest
import io.github.zyrouge.symphony.Symphony

enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE;

    val isPortrait: Boolean get() = this == PORTRAIT
    val isLandscape: Boolean get() = this == LANDSCAPE

    companion object {
        fun fromConfiguration(configuration: Configuration) = when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE
            else -> PORTRAIT
        }

        fun fromConstraints(constraints: BoxWithConstraintsScope) =
            fromDimension(constraints.maxHeight, constraints.maxWidth)

        fun fromDimension(height: Dp, width: Dp) = when {
            width.value > height.value -> LANDSCAPE
            else -> PORTRAIT
        }
    }
}

fun createGrooveArtworkImageRequest(symphony: Symphony, uri: Uri?) = createHandyImageRequestBuilder(
    symphony.applicationContext,
    image = uri ?: Assets.getPlaceholderUri(symphony),
    fallback = Assets.placeholderDarkId,
).build()

fun createGrooveArtworkImageRequests(symphony: Symphony, uris: List<Uri?>) =
    uris.map { createGrooveArtworkImageRequest(symphony, it) }

fun createHandyImageRequestBuilder(context: Context, image: Any, fallback: Int) =
    createHandyImageRequestBuilder(context, image, fallbackResId = fallback)

private fun createHandyImageRequestBuilder(
    context: Context,
    image: Any,
    fallbackResId: Int? = null,
) = ImageRequest.Builder(context).apply {
    data(image)
    fallbackResId?.let {
        placeholder(it)
        fallback(it)
        error(it)
    }
    crossfade(true)
}

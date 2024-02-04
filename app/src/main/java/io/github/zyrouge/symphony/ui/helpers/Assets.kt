package io.github.zyrouge.symphony.ui.helpers

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import coil.request.ImageRequest
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.theme.isLight
import io.github.zyrouge.symphony.ui.theme.toColorSchemeMode

object Assets {
    val placeholderDarkId = R.raw.placeholder_dark
    val placeholderLightId = R.raw.placeholder_light

    fun getPlaceholderId(isLight: Boolean = false) = when {
        isLight -> placeholderLightId
        else -> placeholderDarkId
    }

    fun getPlaceholderId(symphony: Symphony) = Assets.getPlaceholderId(
        isLight = symphony.settings.getThemeMode().toColorSchemeMode(symphony).isLight(),
    )

    fun getPlaceholderUri(symphony: Symphony) = buildUriOfResource(
        symphony.applicationContext.resources,
        getPlaceholderId(symphony),
    )

    fun createPlaceholderImageRequest(symphony: Symphony) =
        ImageRequest.Builder(symphony.applicationContext)
            .data(Assets.getPlaceholderUri(symphony))

    private fun buildUriOfResource(resources: Resources, resourceId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
    }
}

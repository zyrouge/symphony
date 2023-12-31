package io.github.zyrouge.symphony.ui.helpers

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.theme.isLight
import io.github.zyrouge.symphony.ui.theme.toColorSchemeMode

object Assets {
    val placeholderId = R.raw.placeholder
    val placeholderLightId = R.raw.placeholder_light

    fun getPlaceholderId(light: Boolean = false) = if (light) placeholderLightId else placeholderId

    fun getPlaceholderId(symphony: Symphony) = Assets.getPlaceholderId(
        light = symphony.settings.getThemeMode()
            .toColorSchemeMode(symphony.applicationContext.resources.configuration.isNightModeActive)
            .isLight(),
    )

    fun getPlaceholderUri(symphony: Symphony) = buildUriOfResource(
        symphony.applicationContext.resources,
        getPlaceholderId(symphony),
    )

    private fun buildUriOfResource(resources: Resources, resourceId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
    }
}

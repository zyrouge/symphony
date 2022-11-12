package io.github.zyrouge.symphony.services

import io.github.zyrouge.symphony.BuildConfig
import io.github.zyrouge.symphony.ui.helpers.HttpClient
import io.github.zyrouge.symphony.utils.Logger
import okhttp3.CacheControl
import okhttp3.Request
import org.json.JSONObject

object AppMeta {
    const val appName = "Symphony"
    const val author = "Zyrouge"
    const val githubRepositoryOwner = "zyrouge"
    const val githubRepositoryName = "symphony"
    const val githubProfileUrl = "https://github.com/$githubRepositoryOwner"
    const val githubRepositoryUrl =
        "https://github.com/$githubRepositoryOwner/$githubRepositoryName"

    const val version = "v${BuildConfig.VERSION_NAME}"
    var latestVersion: String? = null
    const val githubLatestReleaseUrl = "$githubRepositoryUrl/releases/latest"

    /**
     * Format: v\[yyyy].\[mm].\[versionCode]
     */
    fun fetchLatestVersion(): String? {
        try {
            val latestReleaseUrl =
                "https://api.github.com/repos/$githubRepositoryOwner/$githubRepositoryName/releases/latest"
            val req = Request.Builder()
                .url(latestReleaseUrl)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build()
            val res = HttpClient.newCall(req).execute()
            val content = res.body?.string() ?: ""
            val json = JSONObject(content)
            val tagName = json.getString("tag_name")
            val draft = json.getBoolean("draft")
            if (!draft) {
                latestVersion = tagName
                return tagName
            }
        } catch (err: Exception) {
            Logger.warn("Version check failed: $err")
        }
        return null
    }
}

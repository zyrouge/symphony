package io.github.zyrouge.symphony.ui.helpers

import android.util.Log
import io.github.zyrouge.symphony.BuildConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
    suspend fun getLatestVersion(): String? {
        try {
            val latestReleaseUrl =
                "https://api.github.com/repos/$githubRepositoryOwner/$githubRepositoryName/releases/latest"
            val resp = httpClient.get(latestReleaseUrl)
            val content = resp.bodyAsText()
            val json = JSONObject(content)
            val tagName = json.getString("tag_name")
            val draft = json.getBoolean("draft")
            if (!draft) {
                latestVersion = tagName
                return tagName
            }
        } catch (err: Exception) {
            Log.w("SymLog", "Version check failed: $err")
        }
        return null
    }
}

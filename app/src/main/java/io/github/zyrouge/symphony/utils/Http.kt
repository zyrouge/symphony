package io.github.zyrouge.symphony.ui.helpers

import okhttp3.OkHttpClient

val HttpClient = OkHttpClient.Builder().cache(null).build()

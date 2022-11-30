package io.github.zyrouge.symphony.utils

import okhttp3.OkHttpClient

val HttpClient = OkHttpClient.Builder().cache(null).build()

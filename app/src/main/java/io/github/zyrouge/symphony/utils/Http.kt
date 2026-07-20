package io.github.zyrouge.symphony.utils

import okhttp3.OkHttpClient

val DefaultHttpClient = OkHttpClient.Builder().cache(null).build()

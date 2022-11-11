package io.github.zyrouge.symphony.ui.helpers

import io.ktor.client.*
import io.ktor.client.engine.cio.*

val httpClient = HttpClient(CIO)

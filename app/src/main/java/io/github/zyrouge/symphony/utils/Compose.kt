package io.github.zyrouge.symphony.utils

import io.github.zyrouge.symphony.ui.helpers.ViewContext

fun <T> contextWrapped(fn: (ViewContext) -> T) = fn

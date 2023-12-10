package io.github.zyrouge.symphony.utils

import io.github.zyrouge.symphony.ui.helpers.ViewContext

fun <T> wrapInViewContext(fn: (ViewContext) -> T) = fn

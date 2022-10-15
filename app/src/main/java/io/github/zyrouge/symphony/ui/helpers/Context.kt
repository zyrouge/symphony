package io.github.zyrouge.symphony.ui.helpers

import androidx.navigation.NavHostController
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony

data class ViewContext(
    val symphony: Symphony,
    val activity: MainActivity,
    val navController: NavHostController
)

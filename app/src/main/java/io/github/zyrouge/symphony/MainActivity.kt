package io.github.zyrouge.symphony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.zyrouge.symphony.services.PermissionsManager
import io.github.zyrouge.symphony.ui.view.BaseView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionsManager.activate(this)
        val symphony: Symphony by viewModels()
        setContent {
            BaseView(
                symphony = symphony,
                activity = this
            )
        }
    }
}

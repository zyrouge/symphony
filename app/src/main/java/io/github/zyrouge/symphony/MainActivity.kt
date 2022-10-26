package io.github.zyrouge.symphony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.zyrouge.symphony.ui.view.BaseView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val symphony: Symphony by viewModels()
        symphony.permission.handle(this)
        setContent {
            BaseView(
                symphony = symphony,
                activity = this
            )
        }
    }
}

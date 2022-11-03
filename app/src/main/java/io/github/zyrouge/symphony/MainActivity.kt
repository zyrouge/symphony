package io.github.zyrouge.symphony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.zyrouge.symphony.ui.view.BaseView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var gSymphony: Symphony? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashState: SplashViewModel by viewModels()
        if (savedInstanceState == null) {
            installSplashScreen().apply {
                setKeepOnScreenCondition { !splashState.ready.value }
            }
        }

        val symphony: Symphony by viewModels()
        symphony.permission.handle(this)
        gSymphony = symphony
        symphony.ready()
        setContent {
            LaunchedEffect(LocalContext.current) {
                if (!splashState.isReady) {
                    // NOTE: prevents white screen by giving time to draw first frame
                    delay(150)
                    splashState.toReady()
                }
            }

            BaseView(
                symphony = symphony,
                activity = this
            )
        }
    }

    override fun onPause() {
        super.onPause()
        gSymphony?.pause()
    }
}

class SplashViewModel : ViewModel() {
    private val readyFlow = MutableStateFlow(false)
    val ready = readyFlow.asStateFlow()
    val isReady: Boolean
        get() = readyFlow.value

    fun toReady() {
        if (readyFlow.value) return
        viewModelScope.launch { readyFlow.emit(true) }
    }
}

package io.github.zyrouge.symphony

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.zyrouge.symphony.ui.components.ErrorComp

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMessage = intent?.extras?.getString(KEY_ERROR_MESSAGE) ?: "Unknown"
        val errorStackTrace = intent?.extras?.getString(KEY_ERROR_STACK_TRACE) ?: "-"

        setContent {
            ErrorComp(errorMessage, errorStackTrace)
        }
    }

    companion object {
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_ERROR_STACK_TRACE = "error_stack_trace"

        fun start(context: Context, error: Throwable) {
            val intent = Intent(context, ErrorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            intent.putExtra(KEY_ERROR_MESSAGE, error.toString())
            intent.putExtra(KEY_ERROR_STACK_TRACE, error.stackTraceToString())
            context.startActivity(intent)
        }
    }
}

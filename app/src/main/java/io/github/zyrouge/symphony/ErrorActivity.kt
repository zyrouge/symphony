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

        val errorMessage = intent?.extras?.getString(errorMessageKey) ?: "Unknown"
        val errorStackTrace = intent?.extras?.getString(errorStackTraceKey) ?: "-"

        setContent {
            ErrorComp(errorMessage, errorStackTrace)
        }
    }

    companion object {
        const val errorMessageKey = "error_message"
        const val errorStackTraceKey = "error_stack_trace"

        fun start(context: Context, error: Throwable) {
            val intent = Intent(context, ErrorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            intent.putExtra(errorMessageKey, error.toString())
            intent.putExtra(errorStackTraceKey, error.stackTraceToString())
            context.startActivity(intent)
        }
    }
}

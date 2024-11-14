package io.github.zyrouge.symphony.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony

class Permissions(private val symphony: Symphony) {
    data class State(
        val required: List<String>,
        val granted: List<String>,
        val denied: List<String>,
    ) {
        fun hasAll() = denied.isEmpty()
    }

    fun handle(activity: MainActivity) {
        val state = getState(activity)
        if (state.hasAll()) {
            return
        }
        val contract = ActivityResultContracts.RequestMultiplePermissions()
        activity.registerForActivityResult(contract) {}.launch(state.denied.toTypedArray())
    }

    private fun getRequiredPermissions(): List<String> {
        val required = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            required.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return required
    }

    private fun getState(activity: MainActivity): State {
        val required = getRequiredPermissions()
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        required.forEach {
            if (activity.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED) {
                granted.add(it)
            } else {
                denied.add(it)
            }
        }
        return State(required = required, granted = granted, denied = denied)
    }
}

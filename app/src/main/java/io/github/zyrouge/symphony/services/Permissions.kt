package io.github.zyrouge.symphony.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer

enum class PermissionEvents {
    MEDIA_PERMISSION_GRANTED,
}

data class PermissionsState(
    val required: List<String>,
    val granted: List<String>,
    val denied: List<String>,
) {
    fun hasAll() = denied.isEmpty()
}

class PermissionsManager(private val symphony: Symphony) {
    val onUpdate = Eventer<PermissionEvents>()

    fun handle(activity: MainActivity) {
        val state = getState(activity)
        if (state.hasAll()) return
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.count { it.value } > 0) {
                onUpdate.dispatch(PermissionEvents.MEDIA_PERMISSION_GRANTED)
            }
        }.launch(state.denied.toTypedArray())
    }

    private fun getRequiredPermissions(): List<String> {
        val required = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            required.add(Manifest.permission.READ_MEDIA_AUDIO)
            required.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return required
    }

    private fun getState(activity: MainActivity): PermissionsState {
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
        return PermissionsState(
            required = required,
            granted = granted,
            denied = denied,
        )
    }
}

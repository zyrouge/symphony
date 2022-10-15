package io.github.zyrouge.symphony.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.zyrouge.symphony.MainActivity

object PermissionsManager {
    fun activate(activity: MainActivity) {
        val requiredPermissions = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        val pendingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                activity.applicationContext,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (pendingPermissions.isEmpty()) return
        ActivityCompat.requestPermissions(
            activity,
            pendingPermissions.toTypedArray(),
            1
        )
    }
}
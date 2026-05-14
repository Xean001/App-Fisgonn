package com.example.fisgon.presentation.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberAndroidPermissionsController(): PermissionsController {
    val activity = LocalContext.current.findActivity()
    val controller = remember(activity) { AndroidPermissionsController(activity) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { controller.refresh() }
    val fineLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { controller.refresh() }
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { controller.refresh() }

    controller.requestCameraAction = {
        cameraLauncher.launch(Manifest.permission.CAMERA)
    }
    controller.requestFineLocationAction = {
        fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    controller.requestBackgroundLocationAction = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            controller.refresh()
        }
    }

    return controller
}

private class AndroidPermissionsController(
    private val activity: Activity
) : PermissionsController {
    var requestCameraAction: () -> Unit = {}
    var requestFineLocationAction: () -> Unit = {}
    var requestBackgroundLocationAction: () -> Unit = {}

    override var cameraStatus by mutableStateOf(activity.statusOf(Manifest.permission.CAMERA))
        private set
    override var fineLocationStatus by mutableStateOf(activity.statusOf(Manifest.permission.ACCESS_FINE_LOCATION))
        private set
    override var backgroundLocationStatus by mutableStateOf(readBackgroundLocationStatus())
        private set

    override val canRequestBackgroundLocation: Boolean
        get() = fineLocationStatus == PermissionGrantStatus.Granted

    override fun requestCamera() = requestCameraAction()

    override fun requestFineLocation() = requestFineLocationAction()

    override fun requestBackgroundLocation() {
        if (canRequestBackgroundLocation) requestBackgroundLocationAction()
    }

    fun refresh() {
        cameraStatus = activity.statusOf(Manifest.permission.CAMERA)
        fineLocationStatus = activity.statusOf(Manifest.permission.ACCESS_FINE_LOCATION)
        backgroundLocationStatus = readBackgroundLocationStatus()
    }

    private fun readBackgroundLocationStatus(): PermissionGrantStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return PermissionGrantStatus.Granted
        return activity.statusOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}

private fun Context.statusOf(permission: String): PermissionGrantStatus =
    if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
        PermissionGrantStatus.Granted
    } else {
        PermissionGrantStatus.Missing
    }

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("No Activity found for permission requests")
    }

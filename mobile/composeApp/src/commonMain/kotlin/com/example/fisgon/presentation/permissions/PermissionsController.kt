package com.example.fisgon.presentation.permissions

enum class PermissionGrantStatus {
    Granted,
    Missing
}

interface PermissionsController {
    val cameraStatus: PermissionGrantStatus
    val fineLocationStatus: PermissionGrantStatus
    val backgroundLocationStatus: PermissionGrantStatus
    val canRequestBackgroundLocation: Boolean

    fun requestCamera()
    fun requestFineLocation()
    fun requestBackgroundLocation()
}

object NoopPermissionsController : PermissionsController {
    override val cameraStatus = PermissionGrantStatus.Granted
    override val fineLocationStatus = PermissionGrantStatus.Granted
    override val backgroundLocationStatus = PermissionGrantStatus.Granted
    override val canRequestBackgroundLocation = true

    override fun requestCamera() = Unit
    override fun requestFineLocation() = Unit
    override fun requestBackgroundLocation() = Unit
}

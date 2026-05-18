package com.example.fisgon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.fisgon.data.location.AndroidLocationProvider
import com.example.fisgon.data.repository.AuthRepositoryImpl
import com.example.fisgon.data.repository.PanicRepositoryImpl
import com.example.fisgon.presentation.permissions.rememberAndroidPermissionsController
import com.example.fisgon.services.GeofenceNotificationHelper
import com.example.fisgon.services.GeofenceSyncWorker
import org.maplibre.android.MapLibre
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val locationProvider by lazy { AndroidLocationProvider(this) }
    private val panicRepository by lazy { PanicRepositoryImpl() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(applicationContext)
        GeofenceNotificationHelper.createChannel(this)

        val authRepository = AuthRepositoryImpl()

        setContent {
            val permissionsController = rememberAndroidPermissionsController()
            App(
                authRepository = authRepository,
                permissionsController = permissionsController,
                locationRepository = locationProvider,
                panicRepository = panicRepository,
                onOpenUrl = { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        }
    }

    fun scheduleGeofenceSync(token: String) {
        val data = workDataOf(GeofenceSyncWorker.KEY_TOKEN to token)
        val request = PeriodicWorkRequestBuilder<GeofenceSyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(data)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "geofence_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

package com.example.fisgon.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fisgon.data.remote.ApiConfig
import com.example.fisgon.shared.model.GeofenceDto
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GeofenceSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TOKEN = "token"
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val token = inputData.getString(KEY_TOKEN) ?: return Result.failure()

        val geofences = fetchGeofences(token) ?: return Result.retry()

        if (geofences.isEmpty()) return Result.success()

        val geofencingClient = LocationServices.getGeofencingClient(context)

        val androidGeofences = geofences.map { dto ->
            Geofence.Builder()
                .setRequestId(dto.name)
                .setCircularRegion(dto.latitude, dto.longitude, dto.radiusMeters.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(androidGeofences)
            .build()

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            pendingIntentFlags
        )

        geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener {
            geofencingClient.addGeofences(request, pendingIntent)
        }

        return Result.success()
    }

    private suspend fun fetchGeofences(token: String): List<GeofenceDto>? {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return runCatching {
            client.get("${ApiConfig.BASE_URL}/geofences") {
                bearerAuth(token)
            }.body<List<GeofenceDto>>()
        }.also { client.close() }.getOrNull()
    }
}

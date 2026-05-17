package com.example.fisgon.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object GeofenceNotificationHelper {

    private const val CHANNEL_ID = "fisgon_geofence"
    private const val CHANNEL_NAME = "Alertas de Zona de Riesgo"
    private var notificationId = 1000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando ingresas a una zona de riesgo"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showGeofenceAlert(context: Context, geofenceName: String) {
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Zona de Riesgo Detectada")
            .setContentText("Has ingresado a: $geofenceName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId++, notification)
    }

    fun showPanicAlert(context: Context, latitude: Double, longitude: Double) {
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("ALERTA SOS CERCANA")
            .setContentText("Alerta de pánico en: ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId++, notification)
    }
}

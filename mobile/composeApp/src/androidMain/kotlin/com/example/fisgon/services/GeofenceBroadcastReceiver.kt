package com.example.fisgon.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) {
            val errorMsg = GeofenceStatusCodes.getStatusCodeString(event.errorCode)
            android.util.Log.e("GeofenceBroadcastReceiver", "Geofence error: $errorMsg")
            return
        }

        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = event.triggeringGeofences ?: return
            for (geofence in triggeringGeofences) {
                GeofenceNotificationHelper.showGeofenceAlert(context, geofence.requestId)
            }
        }
    }
}

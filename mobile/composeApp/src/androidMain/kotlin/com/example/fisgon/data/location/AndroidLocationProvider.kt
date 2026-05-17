package com.example.fisgon.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.fisgon.domain.entity.LocationData
import com.example.fisgon.domain.repository.LocationRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidLocationProvider(context: Context) : LocationRepository {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<LocationData?>(null)
    override val location: StateFlow<LocationData?> = _location.asStateFlow()

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
        .setMinUpdateDistanceMeters(10f)
        .build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                _location.value = LocationData(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy = loc.accuracy
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startTracking() {
        client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    }

    override fun stopTracking() {
        client.removeLocationUpdates(callback)
    }
}

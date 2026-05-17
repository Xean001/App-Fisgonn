package com.example.fisgon.domain.entity

data class Geofence(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val severity: Int = 1
)

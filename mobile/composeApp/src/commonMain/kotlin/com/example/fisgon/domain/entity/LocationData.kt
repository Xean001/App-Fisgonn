package com.example.fisgon.domain.entity

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
)

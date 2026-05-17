package com.example.fisgon.domain.entity

data class PanicAlert(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

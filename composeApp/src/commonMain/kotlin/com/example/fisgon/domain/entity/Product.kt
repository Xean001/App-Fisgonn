package com.example.fisgon.domain.entity

data class Product(
    val id: Long = 0,
    val codigo: String,
    val descripcion: String,
    val precio: Double,
    val marca: String
)

package com.example.fisgon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
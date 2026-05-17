package com.example.fisgon.navigation

import com.example.fisgon.domain.entity.User

sealed class AppScreen {
    object Login : AppScreen()
    object Register : AppScreen()
    data class Permissions(val user: User, val token: String) : AppScreen()
    data class Home(val user: User, val token: String) : AppScreen()
}

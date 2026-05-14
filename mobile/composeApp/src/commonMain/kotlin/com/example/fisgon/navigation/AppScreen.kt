package com.example.fisgon.navigation

import com.example.fisgon.domain.entity.User

sealed class AppScreen {
    object Login : AppScreen()
    object Register : AppScreen()
    data class Permissions(val user: User) : AppScreen()
    data class Home(val user: User) : AppScreen()
}

package com.example.fisgon.navigation

import com.example.fisgon.domain.entity.User

sealed class AppScreen {
    object Login : AppScreen()
    data class Token(val user: User, val token: String) : AppScreen()
    data class Home(val user: User) : AppScreen()
}

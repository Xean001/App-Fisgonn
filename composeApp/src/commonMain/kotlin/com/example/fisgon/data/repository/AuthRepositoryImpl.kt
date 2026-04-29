package com.example.fisgon.data.repository

import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.entity.User
import com.example.fisgon.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(credentials: LoginCredentials): Result<User> {
        delay(800)
        return if (credentials.email == "admin@fisgon.com" && credentials.password == "admin123") {
            Result.success(User(1L, "Admin", "Fisgon", credentials.email))
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }

    override suspend fun loginWithGoogle(): Result<User> {
        delay(500)
        return Result.success(User(999L, "Usuario", "Google", "google@gmail.com"))
    }
}

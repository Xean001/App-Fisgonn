package com.example.fisgon.domain.repository

import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.entity.User

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<User>
    suspend fun loginWithGoogle(): Result<User>
}

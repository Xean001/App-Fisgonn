package com.example.fisgon.domain.repository

import com.example.fisgon.domain.entity.AuthSession
import com.example.fisgon.domain.entity.LoginCredentials

interface AuthRepository {
    suspend fun register(credentials: LoginCredentials): Result<AuthSession>
    suspend fun login(credentials: LoginCredentials): Result<AuthSession>
    suspend fun loginWithGoogle(): Result<AuthSession>
}

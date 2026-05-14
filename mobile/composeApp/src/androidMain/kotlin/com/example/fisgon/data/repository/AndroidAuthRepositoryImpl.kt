package com.example.fisgon.data.repository

import com.example.fisgon.domain.entity.AuthSession
import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.repository.AuthRepository

class AndroidAuthRepositoryImpl(
    private val remote: AuthRepository = AuthRepositoryImpl()
) : AuthRepository {

    override suspend fun register(credentials: LoginCredentials): Result<AuthSession> =
        remote.register(credentials)

    override suspend fun login(credentials: LoginCredentials): Result<AuthSession> =
        remote.login(credentials)

    override suspend fun loginWithGoogle(): Result<AuthSession> =
        remote.loginWithGoogle()
}

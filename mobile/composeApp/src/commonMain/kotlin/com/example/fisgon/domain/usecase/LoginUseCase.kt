package com.example.fisgon.domain.usecase

import com.example.fisgon.domain.entity.AuthSession
import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<AuthSession> {
        if (credentials.email.isBlank())
            return Result.failure(Exception("El correo no puede estar vacío"))
        if (!credentials.email.contains("@"))
            return Result.failure(Exception("Formato de correo inválido"))
        if (credentials.password.isBlank())
            return Result.failure(Exception("La contraseña no puede estar vacía"))
        if (credentials.password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        return repository.login(credentials)
    }
}

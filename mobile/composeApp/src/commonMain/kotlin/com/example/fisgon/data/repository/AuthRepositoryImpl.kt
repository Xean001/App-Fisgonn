package com.example.fisgon.data.repository

import com.example.fisgon.data.remote.ApiConfig
import com.example.fisgon.data.remote.createHttpClient
import com.example.fisgon.domain.entity.AuthSession
import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.entity.User
import com.example.fisgon.domain.repository.AuthRepository
import com.example.fisgon.shared.model.AuthResponse
import com.example.fisgon.shared.model.LoginRequest
import com.example.fisgon.shared.model.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthRepositoryImpl(
    private val baseUrl: String = ApiConfig.BASE_URL,
    private val client: HttpClient = createHttpClient()
) : AuthRepository {

    override suspend fun register(credentials: LoginCredentials): Result<AuthSession> =
        runCatching {
            val email = credentials.email.trim()
            val response: HttpResponse = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, credentials.password, credentials.nombre, credentials.apellido))
            }
            response.throwIfError()
            response.body<AuthResponse>().toSession()
        }.recoverCatching { e ->
            throw mapAuthError(e)
        }

    override suspend fun login(credentials: LoginCredentials): Result<AuthSession> =
        runCatching {
            val email = credentials.email.trim()
            val response: HttpResponse = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, credentials.password))
            }
            response.throwIfError()
            response.body<AuthResponse>().toSession()
        }.recoverCatching { e ->
            throw mapAuthError(e)
        }

    override suspend fun loginWithGoogle(): Result<AuthSession> {
        return Result.failure(Exception("Login con Google no disponible"))
    }

    private fun AuthResponse.toSession(): AuthSession {
        val nombre = user.nombre ?: "Usuario"
        val apellido = user.apellido ?: ""
        return AuthSession(
            user = User(
                id = user.id,
                nombre = nombre,
                apellido = apellido,
                email = user.email
            ),
            token = token
        )
    }

    private fun HttpResponse.throwIfError() {
        if (!status.isSuccess()) throw ClientRequestException(this, status.description)
    }

    private fun mapAuthError(error: Throwable): Exception {
        if (error is ClientRequestException) {
            return when (error.response.status) {
                HttpStatusCode.Conflict -> Exception("El usuario ya existe")
                HttpStatusCode.Unauthorized -> Exception("Correo o contraseña incorrectos")
                HttpStatusCode.BadRequest -> Exception("Datos inválidos")
                else -> Exception("Error de autenticación")
            }
        }
        val msg = error.message ?: ""
        return when {
            "timeout" in msg.lowercase() || "ConnectTimeout" in msg || "SocketTimeout" in msg ->
                Exception("Sin conexión al servidor. Verifica que el backend esté activo.")
            "refused" in msg.lowercase() || "Connection refused" in msg ->
                Exception("No se pudo conectar al servidor.")
            "UnknownHostException" in msg || "Unable to resolve" in msg ->
                Exception("Sin acceso a la red.")
            else -> Exception("Error de red. Intenta de nuevo.")
        }
    }
}

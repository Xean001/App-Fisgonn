package com.example.fisgon.data.repository

import com.example.fisgon.data.db.FisgonDatabaseHelper
import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.entity.User
import com.example.fisgon.domain.repository.AuthRepository

class AndroidAuthRepositoryImpl(private val db: FisgonDatabaseHelper) : AuthRepository {

    override suspend fun login(credentials: LoginCredentials): Result<User> {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, nombre, apellido, email FROM users WHERE email = ? AND password = ?",
            arrayOf(credentials.email, credentials.password)
        )
        return if (cursor.moveToFirst()) {
            val user = User(
                id       = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                nombre   = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
                email    = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            )
            cursor.close()
            Result.success(user)
        } else {
            cursor.close()
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }

    override suspend fun loginWithGoogle(): Result<User> =
        Result.success(User(999L, "Usuario", "Google", "google@gmail.com"))
}

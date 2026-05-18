package com.example.fisgon.backend.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordHasher {
    private val secureRandom = SecureRandom()

    fun newSalt(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(saltBytes)
        val hashed = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashed)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }
}

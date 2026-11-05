package com.example.rekly.domain.rules

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray(Charsets.UTF_8))
            .toHex()

    fun matches(password: String, salt: String, storedHash: String): Boolean =
        hash(password, salt) == storedHash

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

package com.example.rekly.domain.rules

import java.security.MessageDigest
import java.security.SecureRandom

object HashSenha {

    private const val BYTES_SALT = 16

    fun gerarSalt(): String {
        val bytes = ByteArray(BYTES_SALT)
        SecureRandom().nextBytes(bytes)
        return bytes.paraHex()
    }

    fun calcular(senha: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + senha).toByteArray(Charsets.UTF_8))
            .paraHex()

    fun confere(senha: String, salt: String, hashSalvo: String): Boolean =
        calcular(senha, salt) == hashSalvo

    private fun ByteArray.paraHex(): String = joinToString("") { "%02x".format(it) }
}

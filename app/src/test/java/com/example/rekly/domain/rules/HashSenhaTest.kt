package com.example.rekly.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HashSenhaTest {

    @Test
    fun mesmo_salt_e_senha_geram_mesmo_hash() {
        assertEquals(HashSenha.calcular("123456", "abc"), HashSenha.calcular("123456", "abc"))
    }

    @Test
    fun mesma_senha_com_salts_diferentes_gera_hashes_diferentes() {
        assertNotEquals(HashSenha.calcular("123456", "abc"), HashSenha.calcular("123456", "xyz"))
    }

    @Test
    fun hash_tem_64_caracteres_hexadecimais() {
        assertTrue(HashSenha.calcular("123456", "abc").matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun hash_nao_contem_a_senha() {
        assertFalse(HashSenha.calcular("minhasenha", "abc").contains("minhasenha"))
    }

    @Test
    fun salts_gerados_sao_diferentes() {
        assertNotEquals(HashSenha.gerarSalt(), HashSenha.gerarSalt())
    }

    @Test
    fun salt_tem_32_caracteres_hexadecimais() {
        assertTrue(HashSenha.gerarSalt().matches(Regex("^[0-9a-f]{32}$")))
    }

    @Test
    fun confere_aceita_a_senha_certa() {
        val salt = HashSenha.gerarSalt()
        val hash = HashSenha.calcular("123456", salt)
        assertTrue(HashSenha.confere("123456", salt, hash))
    }

    @Test
    fun confere_recusa_a_senha_errada() {
        val salt = HashSenha.gerarSalt()
        val hash = HashSenha.calcular("123456", salt)
        assertFalse(HashSenha.confere("654321", salt, hash))
    }
}

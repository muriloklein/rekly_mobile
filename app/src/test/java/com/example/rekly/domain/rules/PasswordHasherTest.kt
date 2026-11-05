package com.example.rekly.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun same_salt_and_password_produce_the_same_hash() {
        assertEquals(PasswordHasher.hash("123456", "abc"), PasswordHasher.hash("123456", "abc"))
    }

    @Test
    fun same_password_with_different_salts_produces_different_hashes() {
        assertNotEquals(PasswordHasher.hash("123456", "abc"), PasswordHasher.hash("123456", "xyz"))
    }

    @Test
    fun hash_has_64_hexadecimal_characters() {
        assertTrue(PasswordHasher.hash("123456", "abc").matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun hash_does_not_contain_the_password() {
        assertFalse(PasswordHasher.hash("mypassword", "abc").contains("mypassword"))
    }

    @Test
    fun generated_salts_are_different() {
        assertNotEquals(PasswordHasher.generateSalt(), PasswordHasher.generateSalt())
    }

    @Test
    fun salt_has_32_hexadecimal_characters() {
        assertTrue(PasswordHasher.generateSalt().matches(Regex("^[0-9a-f]{32}$")))
    }

    @Test
    fun matches_accepts_the_right_password() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("123456", salt)
        assertTrue(PasswordHasher.matches("123456", salt, hash))
    }

    @Test
    fun matches_rejects_the_wrong_password() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("123456", salt)
        assertFalse(PasswordHasher.matches("654321", salt, hash))
    }
}

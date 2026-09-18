package com.application.bibleapp.server.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {
    private val testPassword = "Onezoro22@"

    @Test
    fun verifyingWithTheSamePasswordReturnsTrue() {
        val hash = PasswordHasher.hash(testPassword)

        assertNotEquals(testPassword, hash)
        assertTrue(PasswordHasher.verify(testPassword, hash))
    }

    @Test
    fun verifyingWithAWrongPasswordReturnsFalse() {
        val hash = PasswordHasher.hash(testPassword)

        assertFalse(PasswordHasher.verify("SomeOtherPassword1@", hash))
    }

    @Test
    fun hashingTheSamePasswordTwiceProducesDifferentHashes() {
        val firstHash = PasswordHasher.hash(testPassword)
        val secondHash = PasswordHasher.hash(testPassword)

        assertNotEquals(firstHash, secondHash)
    }
}
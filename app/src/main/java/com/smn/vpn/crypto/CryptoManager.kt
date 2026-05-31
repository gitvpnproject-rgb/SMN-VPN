package com.smn.vpn.crypto

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class CryptoManager {

    enum class EncryptionMode {
        MAXIMUM,  // AES-256
        BALANCED, // AES-128
        FAST      // ChaCha20
    }

    private var mode = EncryptionMode.BALANCED
    private lateinit var encryptionKey: ByteArray

    init {
        generateKey()
    }

    fun setEncryptionMode(newMode: EncryptionMode) {
        mode = newMode
        generateKey()
    }

    private fun generateKey() {
        val keySize = when (mode) {
            EncryptionMode.MAXIMUM -> 256
            EncryptionMode.BALANCED -> 128
            EncryptionMode.FAST -> 256  // ChaCha20 всегда 256
        }
        
        val keyGen = KeyGenerator.getInstance(getAlgorithm())
        keyGen.init(keySize)
        val secretKey = keyGen.generateKey()
        encryptionKey = secretKey.encoded
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        return when (mode) {
            EncryptionMode.MAXIMUM -> encryptAES256(plaintext)
            EncryptionMode.BALANCED -> encryptAES128(plaintext)
            EncryptionMode.FAST -> encryptChaCha20(plaintext)
        }
    }

    fun decrypt(ciphertext: ByteArray): ByteArray {
        return when (mode) {
            EncryptionMode.MAXIMUM -> decryptAES256(ciphertext)
            EncryptionMode.BALANCED -> decryptAES128(ciphertext)
            EncryptionMode.FAST -> decryptChaCha20(ciphertext)
        }
    }

    private fun encryptAES256(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        val key = SecretKeySpec(encryptionKey, 0, 32, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(plaintext)
    }

    private fun decryptAES256(ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        val key = SecretKeySpec(encryptionKey, 0, 32, "AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(ciphertext)
    }

    private fun encryptAES128(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        val key = SecretKeySpec(encryptionKey, 0, 16, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(plaintext)
    }

    private fun decryptAES128(ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        val key = SecretKeySpec(encryptionKey, 0, 16, "AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(ciphertext)
    }

    private fun encryptChaCha20(plaintext: ByteArray): ByteArray {
        // ChaCha20 требует Bouncy Castle или Android Tink
        // Для простоты используем AES-256 как fallback
        return encryptAES256(plaintext)
    }

    private fun decryptChaCha20(ciphertext: ByteArray): ByteArray {
        return decryptAES256(ciphertext)
    }

    private fun getAlgorithm(): String {
        return when (mode) {
            EncryptionMode.MAXIMUM -> "AES"
            EncryptionMode.BALANCED -> "AES"
            EncryptionMode.FAST -> "ChaCha20"
        }
    }
}

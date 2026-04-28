package com.meshcommand.app.comm

import android.util.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-128-CBC encryption for mesh packet payloads.
 * Key must be shared with all firmware nodes (pre-shared key model).
 *
 * Packet format with encryption:
 * [Header 0xAA] [IV 16 bytes] [Encrypted payload] [Footer 0x55]
 */
object PacketEncryption {

    private const val TAG = "PacketEncryption"
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 16 // 128 bits
    private const val IV_SIZE = 16

    /**
     * Encrypt payload with AES-128-CBC.
     * Returns IV + ciphertext.
     */
    fun encrypt(plaintext: ByteArray, key: ByteArray): ByteArray? {
        return try {
            require(key.size == KEY_SIZE) { "Key must be $KEY_SIZE bytes" }

            val iv = ByteArray(IV_SIZE)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key, ALGORITHM),
                IvParameterSpec(iv)
            )

            val encrypted = cipher.doFinal(plaintext)
            iv + encrypted // prepend IV
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}")
            null
        }
    }

    /**
     * Decrypt payload with AES-128-CBC.
     * Input format: IV (16 bytes) + ciphertext.
     */
    fun decrypt(data: ByteArray, key: ByteArray): ByteArray? {
        return try {
            require(key.size == KEY_SIZE) { "Key must be $KEY_SIZE bytes" }
            require(data.size > IV_SIZE) { "Data too short" }

            val iv = data.copyOfRange(0, IV_SIZE)
            val ciphertext = data.copyOfRange(IV_SIZE, data.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, ALGORITHM),
                IvParameterSpec(iv)
            )

            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}")
            null
        }
    }

    /**
     * Generate a random 128-bit key.
     */
    fun generateKey(): ByteArray {
        val key = ByteArray(KEY_SIZE)
        SecureRandom().nextBytes(key)
        return key
    }

    /**
     * Convert key to hex string for display/storage.
     */
    fun keyToHex(key: ByteArray): String {
        return key.joinToString("") { "%02x".format(it) }
    }

    /**
     * Parse hex string back to key bytes.
     */
    fun hexToKey(hex: String): ByteArray {
        require(hex.length == KEY_SIZE * 2) { "Invalid hex key length" }
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}

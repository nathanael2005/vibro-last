package com.nate.app.util

import android.util.Base64
import java.nio.charset.StandardCharsets

object SecurityUtils {
    
    /**
     * Obfuscates a string using XOR and encodes it to Base64.
     */
    fun obfuscate(input: String, key: String): String {
        val inputBytes = input.toByteArray(StandardCharsets.UTF_8)
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        val output = ByteArray(inputBytes.size)
        
        for (i in inputBytes.indices) {
            output[i] = (inputBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        
        return Base64.encodeToString(output, Base64.NO_WRAP)
    }

    /**
     * Decrypts a Base64-XOR obfuscated string.
     * Prevents static scanning of endpoints/keys by reversers.
     */
    fun decrypt(obfuscatedBase64: String, key: String): String {
        return try {
            val decoded = Base64.decode(obfuscatedBase64, Base64.NO_WRAP)
            val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
            val output = ByteArray(decoded.size)
            
            for (i in decoded.indices) {
                output[i] = (decoded[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            
            String(output, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}

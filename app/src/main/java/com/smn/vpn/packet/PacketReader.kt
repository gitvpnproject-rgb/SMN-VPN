package com.smn.vpn.packet

import com.smn.vpn.utils.Logger
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PacketReader {

    fun read(
        packet: SMNPacket,
        encryptionKey: ByteArray,
        hmacKey: ByteArray
    ): ByteArray? {
        try {
            Logger.d("PacketReader", "Reading packet: ID=${packet.packetId}")
            
            // 1. Проверяем HMAC подпись
            val isValid = verifySignature(packet.payload, hmacKey, packet.signature)
            if (!isValid) {
                Logger.e("PacketReader", "Invalid signature! Packet compromised: ID=${packet.packetId}")
                return null
            }
            Logger.d("PacketReader", "Signature verified ✓")
            
            // 2. Расшифровываем данные
            val decrypted = decryptData(packet.payload, encryptionKey)
            Logger.d("PacketReader", "Data decrypted: ${packet.payload.size} → ${decrypted.size} bytes")
            
            Logger.d("PacketReader", "Packet read successfully")
            return decrypted
            
        } catch (e: Exception) {
            Logger.e("PacketReader", "Error reading packet: ${e.message}")
            return null
        }
    }
    
    fun verifyRoute(packet: SMNPacket): Boolean {
        return packet.route.isNotEmpty() && packet.route.size <= 10
    }
    
    fun verifyTTL(packet: SMNPacket): Boolean {
        return packet.ttl > 0
    }
    
    fun isPacketAlive(packet: SMNPacket): Boolean {
        return verifyRoute(packet) && verifyTTL(packet)
    }
    
    private fun verifySignature(
        data: ByteArray,
        key: ByteArray,
        signature: ByteArray
    ): Boolean {
        try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key, "HmacSHA256")
            mac.init(secretKey)
            val computedSignature = mac.doFinal(data)
            return computedSignature.contentEquals(signature)
        } catch (e: Exception) {
            Logger.e("PacketReader", "Verification error: ${e.message}")
            return false
        }
    }
    
    private fun decryptData(data: ByteArray, key: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance("AES")
            val secretKey = SecretKeySpec(key, 0, 32, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            Logger.e("PacketReader", "Decryption error: ${e.message}")
            throw e
        }
    }
}

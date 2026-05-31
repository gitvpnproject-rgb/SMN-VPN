package com.smn.vpn.packet

import com.smn.vpn.utils.Logger
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object PacketBuilder {

    fun build(
        route: List<String>,
        data: ByteArray,
        encryptionKey: ByteArray,
        hmacKey: ByteArray,
        ttl: Int = SMNPacket.DEFAULT_TTL
    ): SMNPacket {
        try {
            Logger.d("PacketBuilder", "Building packet: route=$route, data_size=${data.size}")
            
            // 1. Шифруем данные AES-256
            val encrypted = encryptData(data, encryptionKey)
            Logger.d("PacketBuilder", "Data encrypted: ${data.size} → ${encrypted.size} bytes")
            
            // 2. Создаём HMAC подпись
            val signature = createSignature(encrypted, hmacKey)
            Logger.d("PacketBuilder", "Signature created: ${signature.size} bytes")
            
            // 3. Собираем пакет
            val packet = SMNPacket(
                version = SMNPacket.VERSION,
                route = route,
                ttl = ttl,
                payload = encrypted,
                signature = signature
            )
            
            Logger.d("PacketBuilder", "Packet built successfully: ID=${packet.packetId}")
            return packet
            
        } catch (e: Exception) {
            Logger.e("PacketBuilder", "Error building packet: ${e.message}")
            throw e
        }
    }
    
    private fun encryptData(data: ByteArray, key: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance("AES")
            val secretKey = SecretKeySpec(key, 0, 32, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            Logger.e("PacketBuilder", "Encryption error: ${e.message}")
            throw e
        }
    }
    
    private fun createSignature(data: ByteArray, key: ByteArray): ByteArray {
        try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key, "HmacSHA256")
            mac.init(secretKey)
            return mac.doFinal(data)
        } catch (e: Exception) {
            Logger.e("PacketBuilder", "Signature error: ${e.message}")
            throw e
        }
    }
}

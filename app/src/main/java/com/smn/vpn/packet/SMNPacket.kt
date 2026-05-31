package com.smn.vpn.packet

import java.io.Serializable
import java.nio.ByteBuffer

/**
 * SMN Packet структура:
 * [ VERSION (1 byte) ] [ ROUTE LENGTH (2 bytes) ] [ ROUTE DATA ]
 * [ PAYLOAD LENGTH (4 bytes) ] [ ENCRYPTED PAYLOAD ]
 * [ HMAC SIGNATURE (32 bytes) ]
 */
data class SMNPacket(
    val version: Int = 1,
    val packetId: Long = System.currentTimeMillis(),
    val route: List<String>,  // [Node A, Node B, Node C]
    val ttl: Int = 10,  // Time to live
    val payload: ByteArray,  // Зашифрованные данные
    val signature: ByteArray  // HMAC подпись
) : Serializable {
    
    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(estimateSize())
        
        // Version
        buffer.put(version.toByte())
        
        // Packet ID
        buffer.putLong(packetId)
        
        // TTL
        buffer.put(ttl.toByte())
        
        // Route
        buffer.putShort(route.size.toShort())
        route.forEach { node ->
            val nodeBytes = node.toByteArray(Charsets.UTF_8)
            buffer.putShort(nodeBytes.size.toShort())
            buffer.put(nodeBytes)
        }
        
        // Payload
        buffer.putInt(payload.size)
        buffer.put(payload)
        
        // Signature
        buffer.put(signature)
        
        buffer.flip()
        return buffer.array().copyOfRange(0, buffer.limit())
    }
    
    private fun estimateSize(): Int {
        var size = 1 + 8 + 1 + 2  // version + id + ttl + route_count
        size += route.sumOf { 2 + it.toByteArray().size }  // route data
        size += 4 + payload.size  // payload length + payload
        size += signature.size  // signature
        return size + 100  // buffer
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SMNPacket) return false
        
        if (version != other.version) return false
        if (packetId != other.packetId) return false
        if (route != other.route) return false
        if (ttl != other.ttl) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!signature.contentEquals(other.signature)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = version
        result = 31 * result + packetId.hashCode()
        result = 31 * result + route.hashCode()
        result = 31 * result + ttl
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
    
    companion object {
        const val VERSION = 1
        const val DEFAULT_TTL = 10
    }
}

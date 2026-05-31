package com.smn.vpn.tunnel

import com.smn.vpn.utils.Logger
import java.nio.ByteBuffer

data class IpPacket(
    val version: Int,
    val headerLength: Int,
    val totalLength: Int,
    val ttl: Int,
    val protocol: Int,
    val sourceIp: String,
    val destinationIp: String,
    val payload: ByteArray
)

data class UdpPacket(
    val sourcePort: Int,
    val destinationPort: Int,
    val length: Int,
    val payload: ByteArray
)

data class TcpPacket(
    val sourcePort: Int,
    val destinationPort: Int,
    val sequenceNumber: Long,
    val acknowledgmentNumber: Long,
    val flags: Int,
    val payload: ByteArray
)

class PacketHandler {

    companion object {
        const val PROTOCOL_TCP = 6
        const val PROTOCOL_UDP = 17
        const val PROTOCOL_ICMP = 1
        const val PROTOCOL_IPv6 = 41
    }

    fun parseIpPacket(data: ByteArray): IpPacket? {
        return try {
            if (data.size < 20) {
                Logger.w("PacketHandler", "Packet too small: ${data.size} bytes")
                return null
            }

            val buffer = ByteBuffer.wrap(data)
            
            val versionAndHeaderLength = buffer.get().toInt() and 0xFF
            val version = versionAndHeaderLength shr 4
            val headerLength = (versionAndHeaderLength and 0x0F) * 4
            
            val dscp = buffer.get().toInt() and 0xFF
            val totalLength = buffer.short.toInt() and 0xFFFF
            val identification = buffer.short.toInt() and 0xFFFF
            val flagsAndFragment = buffer.short.toInt() and 0xFFFF
            val ttl = buffer.get().toInt() and 0xFF
            val protocol = buffer.get().toInt() and 0xFF
            val checksum = buffer.short.toInt() and 0xFFFF
            
            val sourceIp = parseIpAddress(buffer)
            val destinationIp = parseIpAddress(buffer)
            
            val payload = ByteArray(data.size - headerLength)
            System.arraycopy(data, headerLength, payload, 0, payload.size)
            
            IpPacket(
                version = version,
                headerLength = headerLength,
                totalLength = totalLength,
                ttl = ttl,
                protocol = protocol,
                sourceIp = sourceIp,
                destinationIp = destinationIp,
                payload = payload
            )
        } catch (e: Exception) {
            Logger.e("PacketHandler", "Error parsing IP packet: ${e.message}")
            null
        }
    }

    fun parseUdpPacket(data: ByteArray): UdpPacket? {
        return try {
            if (data.size < 8) {
                return null
            }

            val buffer = ByteBuffer.wrap(data)
            val sourcePort = buffer.short.toInt() and 0xFFFF
            val destinationPort = buffer.short.toInt() and 0xFFFF
            val length = buffer.short.toInt() and 0xFFFF
            val checksum = buffer.short.toInt() and 0xFFFF
            
            val payload = ByteArray(data.size - 8)
            System.arraycopy(data, 8, payload, 0, payload.size)
            
            UdpPacket(
                sourcePort = sourcePort,
                destinationPort = destinationPort,
                length = length,
                payload = payload
            )
        } catch (e: Exception) {
            Logger.e("PacketHandler", "Error parsing UDP packet: ${e.message}")
            null
        }
    }

    fun parseTcpPacket(data: ByteArray): TcpPacket? {
        return try {
            if (data.size < 20) {
                return null
            }

            val buffer = ByteBuffer.wrap(data)
            val sourcePort = buffer.short.toInt() and 0xFFFF
            val destinationPort = buffer.short.toInt() and 0xFFFF
            val sequenceNumber = buffer.int.toLong() and 0xFFFFFFFFL
            val acknowledgmentNumber = buffer.int.toLong() and 0xFFFFFFFFL
            
            val dataOffsetAndFlags = buffer.short.toInt() and 0xFFFF
            val dataOffset = (dataOffsetAndFlags shr 12) * 4
            val flags = dataOffsetAndFlags and 0x1FF
            
            val window = buffer.short.toInt() and 0xFFFF
            val checksum = buffer.short.toInt() and 0xFFFF
            val urgentPointer = buffer.short.toInt() and 0xFFFF
            
            val payload = ByteArray(maxOf(0, data.size - dataOffset))
            if (payload.isNotEmpty()) {
                System.arraycopy(data, dataOffset, payload, 0, payload.size)
            }
            
            TcpPacket(
                sourcePort = sourcePort,
                destinationPort = destinationPort,
                sequenceNumber = sequenceNumber,
                acknowledgmentNumber = acknowledgmentNumber,
                flags = flags,
                payload = payload
            )
        } catch (e: Exception) {
            Logger.e("PacketHandler", "Error parsing TCP packet: ${e.message}")
            null
        }
    }

    private fun parseIpAddress(buffer: ByteBuffer): String {
        val bytes = ByteArray(4)
        buffer.get(bytes)
        return bytes.joinToString(".") { (it.toInt() and 0xFF).toString() }
    }

    fun getProtocolName(protocol: Int): String {
        return when (protocol) {
            PROTOCOL_TCP -> "TCP"
            PROTOCOL_UDP -> "UDP"
            PROTOCOL_ICMP -> "ICMP"
            PROTOCOL_IPv6 -> "IPv6"
            else -> "Unknown($protocol)"
        }
    }
}

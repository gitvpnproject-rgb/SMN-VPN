package com.smn.vpn.node

import com.smn.vpn.packet.SMNPacket
import com.smn.vpn.packet.PacketReader
import com.smn.vpn.utils.Logger
import kotlin.concurrent.thread

data class NodeInfo(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int = 5555,
    val isAlive: Boolean = true,
    val latency: Long = 0L
)

class SMNNode(
    val nodeInfo: NodeInfo,
    private val encryptionKey: ByteArray,
    private val hmacKey: ByteArray
) {

    private val incomingPackets = mutableListOf<SMNPacket>()
    private val outgoingPackets = mutableListOf<SMNPacket>()
    private var isRunning = false
    private val nextHops = mutableMapOf<String, NodeInfo>()  // узлы соседей

    fun initialize() {
        Logger.d("SMNNode", "Initializing node: ${nodeInfo.name} (${nodeInfo.ip}:${nodeInfo.port})")
        isRunning = true
        startPacketProcessor()
    }

    fun shutdown() {
        Logger.d("SMNNode", "Shutting down node: ${nodeInfo.name}")
        isRunning = false
    }

    fun receivePacket(packet: SMNPacket) {
        if (!isRunning) {
            Logger.w("SMNNode", "Node not running, dropping packet")
            return
        }
        
        Logger.d(
            "SMNNode",
            "Received packet ID=${packet.packetId} from route: ${packet.route.joinToString(" → ")}"
        )
        incomingPackets.add(packet)
    }

    private fun startPacketProcessor() {
        thread {
            while (isRunning) {
                try {
                    if (incomingPackets.isNotEmpty()) {
                        val packet = incomingPackets.removeAt(0)
                        processPacket(packet)
                    }
                    Thread.sleep(10)
                } catch (e: Exception) {
                    Logger.e("SMNNode", "Error in packet processor: ${e.message}")
                }
            }
        }
    }

    private fun processPacket(packet: SMNPacket) {
        Logger.d("SMNNode", "Processing packet ID=${packet.packetId}")
        
        // 1. Проверяем пакет
        if (!PacketReader.isPacketAlive(packet)) {
            Logger.w("SMNNode", "Packet dead: ID=${packet.packetId}")
            return
        }
        
        // 2. Находим себя в маршруте
        val currentNodeIndex = packet.route.indexOf(nodeInfo.id)
        if (currentNodeIndex == -1) {
            Logger.w("SMNNode", "Node not in route: ID=${packet.packetId}")
            return
        }
        
        Logger.d(
            "SMNNode",
            "Node position in route: ${currentNodeIndex + 1}/${packet.route.size}"
        )
        
        // 3. Узел НЕ расшифровывает данные (они защищены HMAC)
        // Узел только пересылает дальше
        
        if (currentNodeIndex < packet.route.size - 1) {
            // Есть следующий узел - пересылаем
            forwardPacket(packet, currentNodeIndex)
        } else {
            // Это последний узел - отправляем в интернет
            exitPacket(packet)
        }
    }

    private fun forwardPacket(packet: SMNPacket, currentIndex: Int) {
        try {
            val nextNodeId = packet.route[currentIndex + 1]
            val nextNode = nextHops[nextNodeId]
            
            if (nextNode == null) {
                Logger.e("SMNNode", "Next node not found: $nextNodeId")
                return
            }
            
            // Уменьшаем TTL
            val newPacket = packet.copy(ttl = packet.ttl - 1)
            
            Logger.d(
                "SMNNode",
                "Forwarding packet ID=${packet.packetId} to ${nextNode.name} (TTL=${newPacket.ttl})"
            )
            
            // Отправляем пакет дальше
            outgoingPackets.add(newPacket)
            
        } catch (e: Exception) {
            Logger.e("SMNNode", "Error forwarding packet: ${e.message}")
        }
    }

    private fun exitPacket(packet: SMNPacket) {
        try {
            // Расшифровываем только на выходном узле
            val plaintext = PacketReader.read(packet, encryptionKey, hmacKey)
            
            if (plaintext != null) {
                Logger.d(
                    "SMNNode",
                    "Packet exiting network ID=${packet.packetId}: ${plaintext.size} bytes"
                )
                // Отправляем в интернет
                sendToInternet(plaintext)
            } else {
                Logger.e("SMNNode", "Failed to decrypt exit packet ID=${packet.packetId}")
            }
            
        } catch (e: Exception) {
            Logger.e("SMNNode", "Error exiting packet: ${e.message}")
        }
    }

    private fun sendToInternet(data: ByteArray) {
        Logger.d("SMNNode", "Sending ${data.size} bytes to internet")
        // TODO: реальная отправка в интернет через socket
    }

    fun addNextHop(node: NodeInfo) {
        nextHops[node.id] = node
        Logger.d("SMNNode", "Added next hop: ${node.name}")
    }

    fun removeNextHop(nodeId: String) {
        nextHops.remove(nodeId)
        Logger.d("SMNNode", "Removed next hop: $nodeId")
    }

    fun getStatus(): String {
        return """
            Node: ${nodeInfo.name}
            IP: ${nodeInfo.ip}:${nodeInfo.port}
            Status: ${if (isRunning) "Running" else "Stopped"}
            Incoming packets: ${incomingPackets.size}
            Outgoing packets: ${outgoingPackets.size}
            Next hops: ${nextHops.size}
        """.trimIndent()
    }
}

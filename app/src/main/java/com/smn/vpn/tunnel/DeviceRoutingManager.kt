package com.smn.vpn.tunnel

import android.net.VpnService
import com.smn.vpn.utils.Logger
import kotlin.concurrent.thread

class DeviceRoutingManager(
    private val vpnService: VpnService
) {

    private lateinit var tunInterface: TunInterface
    private lateinit var trafficRouter: TrafficRouter
    private lateinit var packetHandler: PacketHandler
    
    private var isRunning = false
    private val packetBuffer = mutableListOf<ByteArray>()

    fun initialize(): Boolean {
        return try {
            Logger.d("DeviceRoutingManager", "Initializing routing manager...")
            
            tunInterface = TunInterface(vpnService)
            trafficRouter = TrafficRouter()
            packetHandler = PacketHandler()
            
            if (!tunInterface.establish()) {
                Logger.e("DeviceRoutingManager", "Failed to establish TUN interface")
                return false
            }
            
            Logger.d("DeviceRoutingManager", "Routing manager initialized successfully")
            true
        } catch (e: Exception) {
            Logger.e("DeviceRoutingManager", "Initialization error: ${e.message}")
            false
        }
    }

    fun start() {
        if (isRunning) return
        
        isRunning = true
        Logger.d("DeviceRoutingManager", "Starting traffic routing...")
        
        thread {
            startPacketProcessing()
        }
    }

    fun stop() {
        isRunning = false
        tunInterface.close()
        Logger.d("DeviceRoutingManager", "Traffic routing stopped")
    }

    private fun startPacketProcessing() {
        while (isRunning && tunInterface.isConnected()) {
            try {
                val packet = tunInterface.readPacket()
                if (packet != null && packet.isNotEmpty()) {
                    processPacket(packet)
                }
                Thread.sleep(10)  // Предотвращаем 100% использование CPU
            } catch (e: Exception) {
                Logger.e("DeviceRoutingManager", "Error in packet processing: ${e.message}")
            }
        }
    }

    private fun processPacket(data: ByteArray) {
        try {
            // Парсим IP пакет
            val ipPacket = packetHandler.parseIpPacket(data) ?: return
            
            Logger.d(
                "DeviceRoutingManager",
                "Packet: ${ipPacket.sourceIp} → ${ipPacket.destinationIp} " +
                "(${packetHandler.getProtocolName(ipPacket.protocol)})"
            )
            
            // Находим маршрут для пакета
            val route = trafficRouter.routePacket(ipPacket.sourceIp, ipPacket.destinationIp)
            if (route != null) {
                // Парсим протокол транспортного уровня
                when (ipPacket.protocol) {
                    PacketHandler.PROTOCOL_TCP -> {
                        val tcpPacket = packetHandler.parseTcpPacket(ipPacket.payload)
                        if (tcpPacket != null) {
                            Logger.d(
                                "DeviceRoutingManager",
                                "TCP: ${ipPacket.sourceIp}:${tcpPacket.sourcePort} → " +
                                "${ipPacket.destinationIp}:${tcpPacket.destinationPort}"
                            )
                        }
                    }
                    PacketHandler.PROTOCOL_UDP -> {
                        val udpPacket = packetHandler.parseUdpPacket(ipPacket.payload)
                        if (udpPacket != null) {
                            Logger.d(
                                "DeviceRoutingManager",
                                "UDP: ${ipPacket.sourceIp}:${udpPacket.sourcePort} → " +
                                "${ipPacket.destinationIp}:${udpPacket.destinationPort}"
                            )
                        }
                    }
                }
                
                // Отправляем пакет через TUN интерфейс
                tunInterface.writePacket(data)
            } else {
                Logger.w("DeviceRoutingManager", "No route found for ${ipPacket.destinationIp}")
            }
        } catch (e: Exception) {
            Logger.e("DeviceRoutingManager", "Packet processing error: ${e.message}")
        }
    }

    fun addRoute(destination: String, gateway: String, netmask: String = "255.255.255.255") {
        trafficRouter.addRoute(destination, gateway, netmask)
        Logger.d("DeviceRoutingManager", "Route added: $destination")
    }

    fun removeRoute(destination: String) {
        trafficRouter.removeRoute(destination)
        Logger.d("DeviceRoutingManager", "Route removed: $destination")
    }

    fun printRoutingTable() {
        trafficRouter.printRoutingTable()
    }

    fun isActive(): Boolean = isRunning && tunInterface.isConnected()
}

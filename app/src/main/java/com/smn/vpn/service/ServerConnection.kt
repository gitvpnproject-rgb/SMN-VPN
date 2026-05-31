package com.smn.vpn.service

import android.content.Context
import android.net.ParcelFileDescriptor
import com.smn.vpn.crypto.CryptoManager
import com.smn.vpn.utils.Logger
import java.net.Socket
import kotlin.concurrent.thread

data class Server(
    val id: String,
    val ip: String,
    val port: Int = 443,
    val name: String = "Server"
)

class ServerConnection(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val vpnInterface: ParcelFileDescriptor
) {

    private val servers = mutableListOf<Server>()
    private val sockets = mutableListOf<Socket>()
    private var isConnected = false
    private var activeServer: Server? = null

    init {
        // Инициализируем список серверов (примеры)
        // В реальном приложении они будут загружаться из конфигурации
        servers.add(Server("server1", "10.0.0.1", 443, "US Server"))
        servers.add(Server("server2", "10.0.0.2", 443, "EU Server"))
        servers.add(Server("server3", "10.0.0.3", 443, "Asia Server"))
    }

    fun connect() {
        thread {
            try {
                Logger.d("ServerConnection", "Connecting to 3 servers...")
                
                // Подключаемся к 3 серверам одновременно
                for (i in 0 until minOf(3, servers.size)) {
                    val server = servers[i]
                    try {
                        val socket = Socket(server.ip, server.port)
                        sockets.add(socket)
                        Logger.d("ServerConnection", "Connected to ${server.name}")
                        
                        // Первый активный сервер
                        if (activeServer == null) {
                            activeServer = server
                        }
                    } catch (e: Exception) {
                        Logger.e("ServerConnection", "Failed to connect to ${server.name}: ${e.message}")
                    }
                }
                
                if (activeServer != null) {
                    isConnected = true
                    startTrafficRouting()
                    Logger.d("ServerConnection", "3-server connection established")
                } else {
                    Logger.e("ServerConnection", "Could not connect to any server")
                }
                
            } catch (e: Exception) {
                Logger.e("ServerConnection", "Connection error: ${e.message}")
            }
        }
    }

    fun disconnect() {
        isConnected = false
        sockets.forEach { socket ->
            try {
                socket.close()
            } catch (e: Exception) {
                Logger.e("ServerConnection", "Error closing socket: ${e.message}")
            }
        }
        sockets.clear()
        Logger.d("ServerConnection", "Disconnected from all servers")
    }

    private fun startTrafficRouting() {
        thread {
            while (isConnected && sockets.isNotEmpty()) {
                try {
                    // Маршрутизируем трафик через активный сервер
                    activeServer?.let { server ->
                        Logger.d("ServerConnection", "Routing traffic through ${server.name}")
                        Thread.sleep(1000) // Имитация обработки
                    }
                } catch (e: Exception) {
                    Logger.e("ServerConnection", "Routing error: ${e.message}")
                    // Переключаемся на резервный сервер
                    switchToBackupServer()
                }
            }
        }
    }

    private fun switchToBackupServer() {
        Logger.d("ServerConnection", "Switching to backup server...")
        
        // Находим следующий активный сервер
        val currentIndex = servers.indexOf(activeServer)
        for (i in currentIndex + 1 until servers.size) {
            if (sockets.size > i) {
                activeServer = servers[i]
                Logger.d("ServerConnection", "Switched to ${servers[i].name}")
                return
            }
        }
    }

    fun addServer(ip: String, port: Int = 443, name: String = "Custom Server"): Boolean {
        return try {
            val server = Server("custom_${System.currentTimeMillis()}", ip, port, name)
            servers.add(server)
            Logger.d("ServerConnection", "Server added: $name")
            true
        } catch (e: Exception) {
            Logger.e("ServerConnection", "Failed to add server: ${e.message}")
            false
        }
    }

    fun getActiveServer(): Server? = activeServer

    fun isConnected(): Boolean = isConnected

    fun getServers(): List<Server> = servers.toList()
}
